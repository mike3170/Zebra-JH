package com.stit.model;

import java.util.Objects;

public class EmpPswd {
	private String empNo;
	private String chgPswd;

	public String getEmpNo() {
		return empNo;
	}

	public void setEmpNo(String empNo) {
		this.empNo = empNo;
	}

	public String getChgPswd() {
		return chgPswd;
	}

	public void setChgPswd(String chgPswd) {
		this.chgPswd = chgPswd;
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
		final EmpPswd other = (EmpPswd) obj;
		if (!Objects.equals(this.empNo, other.empNo)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PwdEmpPswd{" + "empNo=" + empNo + ", chgPswd=" + chgPswd + '}';
	}

	
	
}
