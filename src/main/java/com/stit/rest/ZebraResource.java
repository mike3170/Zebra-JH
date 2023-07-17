package com.stit.rest;

import com.stit.common.ApiResponse;
import com.stit.common.ApiResponse.Error;
import com.stit.common.ApiResponse.Status;
import com.stit.model.Loc;
import com.stit.model.WirLoc;
import com.stit.svc.ZebraService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = "/api/zebra")
public class ZebraResource {

	private final Logger log = LogManager.getLogger();
	private static final String WIN_UPLOADED_FOLDER = "C:/Users/user/apps/zebra/upload/";
	private static final String LINUX_UPLOADED_FOLDER = "/home/zebra/upload/";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private ZebraService zebraService;

	/**
	 * login
	 *
	 * @param empNo requestParam
	 * @param chgPswd
	 * @return
	 */
	@RequestMapping(path = "login", method = RequestMethod.GET)
	public ApiResponse login(@RequestParam String empNo, @RequestParam String chgPswd) {
		// log.info("empNo:" + empNo);
		// log.info("chgPswd:" + chgPswd);

		try {
			boolean login = zebraService.login(empNo, chgPswd);
			if (login) {
				return ApiResponse.ok("登入成功!");
				//return new ApiResponse(Status.OK, null, new Error(0, "登入成功!"));
			} else {
				return ApiResponse.error("登入失敗!");
				//return new ApiResponse(Status.ERROR, new Error(-1, "登入失敗!"));
			}
		} catch (Exception ex) {
			log.error("err", ex);
			//return new ApiResponse(Status.ERROR, new Error(-1, ex.getMessage()));
			return ApiResponse.error(ex.getMessage());
		}
	}

	/**
	 * 線材 get wir location list
	 *
	 * @return
	 */
	@RequestMapping(path = "wirLocList", method = RequestMethod.GET)
	public ApiResponse getWirLocList() {
		try {
			List<Loc> locList = zebraService.getWirLocList();
			return new ApiResponse(Status.OK, locList, new Error(0, null));

		} catch (Exception ex) {
			log.error("err", ex);
			return new ApiResponse(Status.ERROR, new Error(-1, ex.getMessage()));
		}
	}

	/**
	 * 螺絲鐵桶, 剪力釘, 螺帽, 外購, 組合品, 成品棧板, 餘料 get screw location list
	 *
	 * @return
	 */
	@RequestMapping(path = "screwLocList", method = RequestMethod.GET)
	public ApiResponse getScrewLocList() {
		try {
			List<Loc> locList = zebraService.getScrewLocList();
			return new ApiResponse(Status.OK, locList, new Error(0, null));

		} catch (Exception ex) {
			log.error("err", ex);
			return new ApiResponse(Status.ERROR, new Error(-1, ex.getMessage()));
		}
	}

	@PostMapping("/upload") // //new annotation since 4.3
	public ApiResponse singleFileUpload(@RequestParam("file") MultipartFile file) {
		int insertRowCount = 0;

		if (file.isEmpty()) {
			log.error("api-空檔");
			return new ApiResponse(Status.ERROR, new Error(-1, "空檔!"));
		}

		String upload_foler = null;
		if (this.isWindow()) {
			upload_foler = WIN_UPLOADED_FOLDER;
		} else {
			upload_foler = LINUX_UPLOADED_FOLDER;
		}

		//log.trace("aaa");
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss-SSS");
		String postFix = dtFmt.format(now);

		String fileName = upload_foler + file.getOriginalFilename() + "-" + postFix;
		//log.trace("bbb");
		log.trace("fileName: " + fileName);

		try {
			byte[] bytes = file.getBytes();
			Path path = Paths.get(fileName);
			Files.write(path, bytes);
			//log.trace("ccc");

			List<String> lines = Files.readAllLines(path);
			if (lines.size() == 0) {
				log.error("api", "空檔!");
				return new ApiResponse(Status.ERROR, new Error(-1, "空檔!"));
			}

			//log.trace("ddd");
			insertRowCount = this.zebraService.insertTemp(lines);

		} catch (Exception ex) {
			ex.printStackTrace();
			//log.trace("eee");
			log.error("api-mia", ex);
			return new ApiResponse(Status.ERROR, new Error(-1, ex.getMessage()));
		}

		log.trace("-------------------------------------");
		log.debug("insert row count:" + insertRowCount);
		return new ApiResponse(Status.OK, insertRowCount);
		// return new ApiResponse(Status.OK, new Error(0, String.valueOf(insertRowCount)));
	}

	// 
	private boolean isWindow() {
		String osName = System.getProperty("os.name");
		return osName.toLowerCase().contains("windows");
	}

} // end class
