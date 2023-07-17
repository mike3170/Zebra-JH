package com.stit.rest;

import com.stit.common.ApiResponse;
import com.stit.common.ApiResponse.Error;
import com.stit.common.ApiResponse.Status;
import com.stit.common.Pair;
import com.stit.model.AskCoilScanTemp;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
//import com.stit.entity.cod.CodMast;
//import com.stit.repo.cod.CodMastRepo;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping(path = "/api/test")
public class TestResource {

	private final Logger log = LogManager.getLogger();

	@Autowired
	private JdbcTemplate jdbc;

	//@Autowired
	//private CodMastRepo codMastRepo;

	//@RequestMapping(path = "findAll", method = RequestMethod.GET)
	//public ApiResponse findAll() {
	//	log.trace("CodMastReousce - findAll ...");
	//	try {
	//		List<CodMast> dtoList = this.codMastRepo.findAll();

	//		return new ApiResponse(Status.OKAY, dtoList);
	//	} catch (Exception ex) {
	//		return new ApiResponse(Status.ERROR, null, new Info(-1, ex.getMessage()));
	//	}
	//}

	@RequestMapping(path = "mia", method = RequestMethod.GET)
	public ApiResponse mia() {
		log.trace("mia - test ...");
		try {
			//List<CodMast> dtoList = this.codMastRepo.findAll();
			Pair<String, String> pair = new Pair<>("oracle", "甲骨文");
			

			return new ApiResponse(Status.OK, pair);
		} catch (Exception ex) {
			return new ApiResponse(Status.ERROR,  new Error(-1, ex.getMessage()));
		}
	}

	@GetMapping("date")
	public ApiResponse test() {
		log.trace("mia - test ...");
		
		//LocalDate myDate = LocalDate.ofEpochDay(1581404947000L);
		SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		java.util.Date date = new Date(1581413246000L);
		log.info(date);
		log.info("date: " + dateFmt.format(date));


		try {
			String sql = "select * from ask_coil_scan_temp where " +
			" kind = 1 order by scan_date desc";

		//String[] params = new String[]{
		//	kind,
		//	strDateSlash
		//};	

		List<AskCoilScanTemp> list = jdbc.query(sql, (ResultSet rs, int i) -> {
				AskCoilScanTemp scanTemp = new AskCoilScanTemp();
				scanTemp.setProcEmp(rs.getString("proc_emp"));
				scanTemp.setSheetNo(rs.getString("sheet_no"));
				scanTemp.setSheetDate(rs.getDate("sheet_date"));
				scanTemp.setScanDate(rs.getDate("scan_date"));
				scanTemp.setKind(rs.getString("kind"));
				scanTemp.setBarCode(rs.getString("bar_code"));
				scanTemp.setLocate(rs.getString("locate"));
				scanTemp.setScwJobNo(rs.getString("scw_job_no"));
				scanTemp.setItemNo(rs.getInt("item_no"));
				scanTemp.setIsrtType(rs.getString("isrt_type"));
				scanTemp.setReasonCode(rs.getString("reason_code"));
				scanTemp.setClassNo(rs.getString("class_no"));
				scanTemp.setPassYn(rs.getString("pass_yn"));

				return scanTemp;
			});
			
			return new ApiResponse(Status.OK, list);
		} catch (Exception ex) {
			return new ApiResponse(Status.ERROR,  new Error(-1, ex.getMessage()));
		}
	}

	@GetMapping("proc")
	public ApiResponse doProc() {	
		try {
			this.jdbc.setResultsMapCaseInsensitive(true);
			SimpleJdbcCall jdbCall = new SimpleJdbcCall(this.jdbc)
				.withProcedureName("SP_INSERT_APC2210M_CHK");


			java.util.Date date = new java.util.Date();

			MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("pv_job_no", "job001", Types.VARCHAR)
				.addValue("pn_item_no", 1, Types.NUMERIC)
				.addValue("pv_coil_no", "W12345678", Types.VARCHAR)
				.addValue("pd_proc_date", date, Types.DATE)
				.addValue("pv_proc_emp", "dav", Types.VARCHAR);

			Map<String, Object> outMap = jdbCall.execute(params);

		  return ApiResponse.ok(outMap);

		} catch (Exception e) {
			log.error("err", e);
		}
		return ApiResponse.error("err");


	}


} // end class
