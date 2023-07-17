package com.stit.model;

import java.util.Date;
import java.util.Objects;

public class BarcodeTemp {
	private String procEmp;
 	private String kind;
  private String docNo;
  private String locate;
  private String barCode;
  private String sheetNo;
  private Date sheetDate;
  private Date scanDate;
  private String  scanType;

	public String getProcEmp() {
		return procEmp;
	}

	public void setProcEmp(String procEmp) {
		this.procEmp = procEmp;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getDocNo() {
		return docNo;
	}

	public void setDocNo(String docNo) {
		this.docNo = docNo;
	}

	public String getLocate() {
		return locate;
	}

	public void setLocate(String locate) {
		this.locate = locate;
	}

	public String getBarCode() {
		return barCode;
	}

	public void setBarCode(String barCode) {
		this.barCode = barCode;
	}

	public String getSheetNo() {
		return sheetNo;
	}

	public void setSheetNo(String sheetNo) {
		this.sheetNo = sheetNo;
	}

	public Date getSheetDate() {
		return sheetDate;
	}

	public void setSheetDate(Date sheetDate) {
		this.sheetDate = sheetDate;
	}

	public Date getScanDate() {
		return scanDate;
	}

	public void setScanDate(Date scanDate) {
		this.scanDate = scanDate;
	}

	public String getScanType() {
		return scanType;
	}

	public void setScanType(String scanType) {
		this.scanType = scanType;
	}


	@Override
	public int hashCode() {
		int hash = 3;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BarcodeTemp other = (BarcodeTemp) obj;
		if (!Objects.equals(this.procEmp, other.procEmp)) {
			return false;
		}
		if (!Objects.equals(this.kind, other.kind)) {
			return false;
		}
		if (!Objects.equals(this.barCode, other.barCode)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BarcodeTemp{" + "procEmp=" + procEmp + ", kind=" + kind + ", docNo=" + docNo + ", locate=" + locate + ", barCode=" + barCode + ", sheetNo=" + sheetNo + ", sheetDate=" + sheetDate + '}';
	}

	



	
}
