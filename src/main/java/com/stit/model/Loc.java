package com.stit.model;

import java.util.Objects;

public class Loc {
	private String kind;
	private String locNo;
	private String remark;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getLocNo() {
		return locNo;
	}

	public void setLocNo(String locNo) {
		this.locNo = locNo;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public int hashCode() {
		int hash = 7;
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
		final Loc other = (Loc) obj;
		if (!Objects.equals(this.kind, other.kind)) {
			return false;
		}
		if (!Objects.equals(this.locNo, other.locNo)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Loc{" + "kind=" + kind + ", locNo=" + locNo + ", remark=" + remark + '}';
	}
	
}
