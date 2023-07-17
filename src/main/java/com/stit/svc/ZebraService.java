package com.stit.svc;

import com.stit.model.BarcodeTemp;
import com.stit.model.Loc;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ZebraService {

	private Logger log = LogManager.getLogger();
	private ReentrantLock lock = new ReentrantLock();
	private SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMddHHmmss");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * login
	 *
	 * @param empNo
	 * @param chgPswd
	 * @return
	 */

	public boolean login(String empNo, String chgPswd) {
		String sql 
			= "select count(*) from pwd_emp_pswd where "
			+ " emp_no = ? and chg_pswd = ?";

		int count = jdbcTemplate.queryForObject(sql, new Object[]{empNo, chgPswd}, Integer.class);

		return count == 0 ? false : true;
	}

	/**
	 * 線材
	 *
	 * @return
	 */
	public List<Loc> getWirLocList() {
		String sql = "select * from v_wir_loc order by loc_no, kind";

		List<Loc> locList = jdbcTemplate.query(sql, (ResultSet rs, int i) -> {
			Loc loc = new Loc();
			loc.setKind(rs.getString("kind"));
			loc.setLocNo(rs.getString("loc_no"));
			loc.setRemark(rs.getString("remark"));
			return loc;
		});

		return locList;
	}

	// all except 線材
	public List<Loc> getScrewLocList() {
		String sql = "select * from v_screw_loc order by loc_no, kind";

		List<Loc> locList = jdbcTemplate.query(sql, (ResultSet rs, int i) -> {
			Loc loc = new Loc();
			loc.setKind(rs.getString("kind"));
			loc.setLocNo(rs.getString("loc_no"));
			loc.setRemark(rs.getString("remark"));
			return loc;
		});

		return locList;
	}

	/**
	 * serialize, because of sheetNo, using lock insert into db temp table.
	 *
	 * @param lines
	 * @return
	 */
	public int insertTemp(List<String> lines) {
		final AtomicInteger insertRowCount = new AtomicInteger(0);

		String sql
			= "insert into  ask_bar_code_temp "
			+ " (proc_emp, kind, doc_no, locate, bar_code, sheet_no, sheet_date, scan_date, scan_type) "
			+ " values(?, ?, ?, ?, ?, ?, ?, ?, ?)";

		this.lock.lock();
		try {
			// 上傳日期
			LocalDateTime dt = LocalDateTime.now();
			Date today = java.sql.Timestamp.valueOf(dt);

			// 上傳單號-流水號 yyyymmdd-999
			String sheetNo = this.getNextSheetNo();

			// line to bean
			List<BarcodeTemp> barcodeList = new ArrayList<>();
			lines.forEach((String line) -> {
				log.trace(line);

				BarcodeTemp bean = this.toBean(line, today, sheetNo);
				//if (bean == null) {
				//	throw  new IllegalStateException("上傳檔案格式錯誤!");
				//}

				// 12/19
				if (bean != null) {
					if ((bean.getBarCode() != null) && (!bean.getBarCode().trim().isEmpty())) {
						barcodeList.add(bean);
					}
				}

			});

			// insert db
			barcodeList.forEach(bean -> {
				Object[] params = new Object[]{
					bean.getProcEmp(),
					bean.getKind(),
					bean.getDocNo(),
					bean.getLocate(),
					bean.getBarCode(),
					bean.getSheetNo(),
					bean.getSheetDate(),
					bean.getScanDate(),
					bean.getScanType(),};

				int _count = this.jdbcTemplate.update(sql, params);
				//log.trace("affected count: " + _count);
				if (_count == 0) {
					log.error("service", "新增資料失敗!");
					throw new IllegalStateException("新增資料失敗!");
				}

				insertRowCount.incrementAndGet();

			});
		} catch (Exception ex) {
			throw new IllegalStateException(ex.getMessage());
		} finally {
			this.lock.unlock();
		}

		return insertRowCount.intValue();
	}

	// parse line to BarcodeTemp bean
	private BarcodeTemp toBean(String line, Date today, String sheetNo) {
		BarcodeTemp bct = new BarcodeTemp();

		//String[] fields = line.split(",", 6);
		//if (fields.length != 6) {
		//	log.error("service", "檔案格式錯誤!");
		//	throw new IllegalStateException("檔案格式錯誤!");
		//}
		// update 12/19 for barcode contrain "," character
		String[] fields = line.split(",");
		if (fields.length != 6) {
			//log.error("service", "檔案格式錯誤!");
			//throw new IllegalStateException("檔案格式錯誤!");
			return null;
		}

		String procEmp = fields[0];
		String kind = fields[1];
		String docNo = fields[2];
		String locate = fields[3];
		String barcode = fields[4];
		String _scanDate = fields[5];

		Date scanDate = null;
		try {
			scanDate = this.dateFmt.parse(_scanDate);
		} catch (Exception e) {
			log.error("service", "日期格式錯誤!");
			throw new IllegalArgumentException("日期格式錯誤!");
		}

		bct.setProcEmp(procEmp);
		bct.setKind(kind);
		bct.setDocNo(docNo);
		bct.setLocate(locate);
		bct.setBarCode(barcode);

		bct.setSheetDate(today);
		bct.setSheetNo(sheetNo);
		bct.setScanDate(scanDate);
		bct.setScanType("1");  // pda

		return bct;
	}

	// 上船單號 yyyymmdd-999
	private String getNextSheetNo() {
		String sql = "select max(substr(sheet_no, -3, 3)) seq  "
			+ "from ask_bar_code_temp where "
			+ "substr(sheet_no, 1, 8) = ?";

		LocalDate today = LocalDate.now();
		String tdString = today.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyymmdd
		// log.info(tdString);

		Object[] params = new Object[]{
			tdString
		};

		String seq = this.jdbcTemplate.queryForObject(sql, params, String.class);
		// log.info(seq);
		if (seq == null) {
			seq = tdString + "-001";
		} else {
			int _seq = Integer.parseInt(seq) + 1;
			seq = tdString + "-" + String.format("%03d", _seq);
		}

		return seq;
	}

}  // end class
