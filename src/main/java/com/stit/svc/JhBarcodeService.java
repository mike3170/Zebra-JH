package com.stit.svc;

import com.stit.model.ApcJobOrde;
import com.stit.model.AskCoilScanTemp;
import com.stit.model.CodMast;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

@Service
public class JhBarcodeService {
	private Logger log = LogManager.getLogger();
	private ReentrantLock lock = new ReentrantLock();

	private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMddHHmmss");

	private final SimpleDateFormat dateFmtSlash = new SimpleDateFormat("yyyy/MM/dd");
	private final SimpleDateFormat dateFmtNoSlash = new SimpleDateFormat("yyyyMMdd");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// constructor
	public JhBarcodeService() {
	}

	public List<CodMast> getCodMastList() {
		String sql = "select kind, code_no, code_name from cod_mast where kind in ('AMRS', 'CLAS') ";

		List<CodMast> codMastList = jdbcTemplate.query(sql, (ResultSet rs, int i) -> {
			CodMast codMast = new CodMast();
			codMast.setKind(rs.getString("kind"));
			codMast.setCodeNo(rs.getString("code_no"));
			codMast.setCodeName(rs.getString("code_name"));
			return codMast;
		});

		return codMastList;
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
			= "insert into ask_coil_scan_temp "
			+ " (proc_emp, sheet_no, sheet_date, scan_date, kind, bar_code, "
			+ "  locate, scw_job_no, item_no, isrt_type, reason_code, class_no, pass_yn) "
			+ " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try {
			// 上傳日期
			LocalDateTime dt = LocalDateTime.now();
			Date today = java.sql.Timestamp.valueOf(dt);

			// 上傳單號-流水號 yyyymmdd-999
			String sheetNo = this.getNextSheetNo();

			// line to bean
			List<AskCoilScanTemp> barcodeList = new ArrayList<>();
			lines.forEach((String line) -> {
				log.trace(line);
				AskCoilScanTemp bean = this.toBean(line, today, sheetNo);
				barcodeList.add(bean);

				// 12/19
				// 2020/01/10 because no barcode
				//if (bean != null) {
				//	if ((bean.getBarCode() != null) && (!bean.getBarCode().trim().isEmpty())) {
				//		barcodeList.add(bean);
				//	}
				//}
			});

			// insert db
			barcodeList.forEach(bean -> {
				Object[] params = new Object[]{
					bean.getProcEmp(),
					bean.getSheetNo(),
					bean.getSheetDate(),
					bean.getScanDate(),
					bean.getKind(),
					bean.getBarCode(),
					bean.getLocate(),
					bean.getScwJobNo(),
					bean.getItemNo(),
					bean.getIsrtType(),
					bean.getReasonCode(),
					bean.getClassNo(),
					bean.getPassYn()
				};

				int _count = this.jdbcTemplate.update(sql, params);
				//log.trace("affected count: " + _count);
				if (_count == 0) {
					log.error("service", "新增資料失敗!");
					throw new IllegalStateException("新增資料失敗!");
				}

				insertRowCount.incrementAndGet();

			});   // foreach
		} catch (Exception ex) {
			log.error("err", ex);
			throw new IllegalStateException(ex.getMessage());
		}

		return insertRowCount.intValue();
	}

	// parse line to BarcodeTemp bean
	private AskCoilScanTemp toBean(String line, Date today, String sheetNo) {
		AskCoilScanTemp scanTemp = new AskCoilScanTemp();

		String[] fields = line.split(",", 11);

		if (fields.length != 11) {
			log.error("service", "檔案格式錯誤!");
			throw new IllegalStateException("檔案格式錯誤!");
		}

		String procEmp = fields[0];
		String _scanDate = fields[1];
		String kind = fields[2];
		String barCode = fields[3];
		String locate = fields[4];
		String scwJobNo = fields[5];
		String itemNo = fields[6];
		String isrtType = fields[7];
		String reasonCode = fields[8];
		String classNo = fields[9];
		String passYn = fields[10];

		Date scanDate = null;
		try {
			scanDate = this.dateFmt.parse(_scanDate);
		} catch (Exception e) {
			log.error("service", "日期格式錯誤!");
			throw new IllegalArgumentException("日期格式錯誤!");
		}

		scanTemp.setProcEmp(procEmp);
		scanTemp.setSheetNo(sheetNo);
		scanTemp.setSheetDate(today);
		scanTemp.setScanDate(scanDate);
		scanTemp.setKind(kind);
		scanTemp.setBarCode(barCode);
		scanTemp.setLocate(locate);
		scanTemp.setScwJobNo(scwJobNo);
		scanTemp.setItemNo(Integer.valueOf(itemNo));
		scanTemp.setIsrtType(isrtType);
		scanTemp.setReasonCode(reasonCode);
		scanTemp.setClassNo(classNo);
		scanTemp.setPassYn(passYn);

		return scanTemp;
	}

	/**
	 * online insert if network(android) is okay
	 */
	public Integer insertData(AskCoilScanTemp bean) {
		log.info("insertData");

		int insertCount = 0;

		String sql
			= "insert into ask_coil_scan_temp "
			+ " (proc_emp, sheet_no, sheet_date, scan_date, kind, bar_code, "
			+ "  locate, scw_job_no, item_no, isrt_type, reason_code, class_no, pass_yn) "
			+ " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		// 上傳單號-流水號 yyyymmdd-999
		try {
			String sheetNo = this.getNextSheetNo();
			bean.setSheetNo(sheetNo);

			LocalDateTime dt = LocalDateTime.now();
			Date today = java.sql.Timestamp.valueOf(dt);
			bean.setSheetDate(today);
		} catch (Exception ex) {
			log.error("err", ex);
			throw new IllegalStateException(ex.getMessage());
		} 

		log.info(bean);

		Object[] params = new Object[]{
			bean.getProcEmp(),
			bean.getSheetNo(),
			bean.getSheetDate(),
			bean.getScanDate(),
			bean.getKind(),
			bean.getBarCode(),
			bean.getLocate(),
			bean.getScwJobNo(),
			bean.getItemNo(),
			bean.getIsrtType(),
			bean.getReasonCode(),
			bean.getClassNo(),
			bean.getPassYn()
		};

		try {
			insertCount = this.jdbcTemplate.update(sql, params);
			if (insertCount == 0) {
				log.error("service", "新增資料失敗!");
				throw new IllegalStateException("新增資料失敗!");
			}
		} catch (Exception e) {
			log.error("insertData", e);
		  throw new IllegalStateException(e.getMessage());
		}

		return insertCount;
	}

	// 上傳單號 yyyymmdd-999
	private String getNextSheetNo() {
		String sql = "select max(substr(sheet_no, -3, 3)) seq  "
			+ "from ask_coil_scan_temp where "
			+ "substr(sheet_no, 1, 8) = ?";

		LocalDate today = LocalDate.now();
		String tdString = today.format(DateTimeFormatter.BASIC_ISO_DATE); // yyyymmdd
		// log.info(tdString);

		Object[] params = new Object[]{
			tdString
		};

		this.lock.lock();
		String seq = "";
		try {
			seq = this.jdbcTemplate.queryForObject(sql, params, String.class);
			// log.info(seq);
			if (seq == null) {
				seq = tdString + "-001";
			} else {
				int _seq = Integer.parseInt(seq) + 1;
				seq = tdString + "-" + String.format("%03d", _seq);
			}
		} catch (Exception e) {
			log.error("err", e);
			throw new IllegalStateException(e.getMessage());
		} finally {
			this.lock.unlock();
	  }

		return seq;
	}

	// 工令領用查單用
	public List<ApcJobOrde> getApcJobOrdeList(String jobNo) {
		String sql
			= "select job_no, item_no, wir_kind, draw_dia, assm_no, assm_name,"
			+ "  luo_no, requ_qty, issu_qty, fnsh_qty "
			+ "  from v_apc_job_orde where job_no = ? order by job_no, item_no";

		String[] params = new String[]{
			jobNo
		};

		try {
			List<ApcJobOrde> list = jdbcTemplate.query(sql, params, (ResultSet rs, int i) -> {
				ApcJobOrde bean = new ApcJobOrde();
				bean.setJobNo(rs.getString("job_no"));
				bean.setItemNo(rs.getInt("item_no"));
				bean.setWirKind(rs.getString("wir_kind"));
				bean.setDrawDia(rs.getBigDecimal("draw_dia"));
				bean.setAssmNo(rs.getString("assm_no"));
				bean.setAssmName(rs.getString("assm_Name"));
				bean.setLouNo(rs.getString("luo_no"));
				bean.setRequQty(rs.getBigDecimal("requ_qty"));
				bean.setIssuQty(rs.getBigDecimal("issu_qty"));
				bean.setFnshQty(rs.getBigDecimal("fnsh_qty"));
				return bean;
			});

			return list;

		} catch (Exception e) {
			log.error("err", e);
			throw e;
		}
	}

	/**
	 * 查詢
	 */
	public List<AskCoilScanTemp> getAskCoilScanTemp(String kind, String scanDate) {

		String strDateSlash = this.getStringDateSlash(scanDate);
		log.info("scanDate:" + scanDate);

		String sql
			= "select * from ask_coil_scan_temp where "
			+ " kind = ?  and "
			+ " to_char(scan_date, 'YYYY/MM/DD') = ? order by scan_date";

		String[] params = new String[]{
			kind,
			strDateSlash
		};

		try {
			List<AskCoilScanTemp> list = jdbcTemplate.query(sql, params, (ResultSet rs, int i) -> {
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

			//log.info("size: " + list.size());
			return list;

		} catch (Exception e) {
			log.error("err", e);
			throw e;
		}
	}

	/**
	 * 工令領用 細項刪除
	 *
	 * @param jobNo
	 * @param itemNo
	 * @param barcode
	 * @param scanDate yyyy-mm-dd
	 * @return
	 */
	public boolean deleteJobOder(String kind, String jobNo, int itemNo, String barcode, String scanDate) {
		int updateCount = 0;

		try {
			String sql = "delete from ask_coil_scan_temp where "
				+ " kind       = ? and "
				+ " scw_job_no = ? and "
				+ " item_no    = ? and "
				+ " bar_code   = ? and "
				+ " to_char(scan_date, 'yyyy-mm-dd')  = ?";

			Object[] params = new Object[]{
				kind,
				jobNo,
				itemNo,
				barcode,
				scanDate
			};

			updateCount = this.jdbcTemplate.update(sql, params);
			log.info("delete count:" + updateCount);

		} catch (Exception e) {
			log.error("err", e);
			throw new IllegalArgumentException(e.getMessage());
		}

		return updateCount >= 1;
	}

	public Map<String, Object> jobOrderCheck(String jobNo, String coilNo, String procDate, String procEmp) { 
		log.info("jobNO:" + jobNo);
		// log.info("itemNo:" + itemNo);
		log.info("coilNo:" + coilNo);
		log.info("procDate:" + procDate);
		log.info("procEmp:" + procEmp);

		try {
			this.jdbcTemplate.setResultsMapCaseInsensitive(true);

			SimpleJdbcCall jdbCall = new SimpleJdbcCall(this.jdbcTemplate)
				.withProcedureName("SP_INSERT_APC2210M_CHK");

			MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("pv_job_no", jobNo, Types.VARCHAR)
				.addValue("pn_item_no", 0, Types.NUMERIC)          // itemNo 已不用, 2020-03-07
				.addValue("pv_coil_no", coilNo, Types.VARCHAR)
				.addValue("pd_proc_date", procDate, Types.DATE)
				.addValue("pv_proc_emp", procEmp, Types.VARCHAR);

			Map<String, Object> outMap = jdbCall.execute(params);

			log.info(outMap.toString());

			return outMap;

		} catch (Exception e) {
			log.error("err", e);
		}

		return null;

	}

	private String getStringDateSlash(String strDate) {
		java.util.Date date = null;

		try {
			date = this.dateFmtSlash.parse(strDate);
		} catch (ParseException ex) {
			try {
				date = this.dateFmtNoSlash.parse(strDate);
			} catch (ParseException ex2) {
				log.error("err", ex2);
			}
		}

		return this.dateFmtSlash.format(date);
	}

}  // end class
