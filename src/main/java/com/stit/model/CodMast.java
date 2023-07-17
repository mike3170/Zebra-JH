package com.stit.model;

import java.util.Objects;

public class CodMast {
	private String kind;
	private String codeNo;
	private String codeName;

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getCodeNo() {
		return codeNo;
	}

	public void setCodeNo(String codeNo) {
		this.codeNo = codeNo;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + Objects.hashCode(this.kind);
		hash = 67 * hash + Objects.hashCode(this.codeNo);
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
		final CodMast other = (CodMast) obj;
		if (!Objects.equals(this.kind, other.kind)) {
			return false;
		}
		if (!Objects.equals(this.codeNo, other.codeNo)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "CodMast{" + "kind=" + kind + ", codeNo=" + codeNo + ", codeName=" + codeName + '}';
	}
	
}
