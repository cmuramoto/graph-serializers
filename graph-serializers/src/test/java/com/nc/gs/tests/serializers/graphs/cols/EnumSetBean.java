package com.nc.gs.tests.serializers.graphs.cols;

import java.util.EnumSet;

public class EnumSetBean {

	enum L {
		_000, _001, _002, _003, _004, _005, _006, _007, _008, _009, _010, _011, _012, _013, _014, _015, _016, _017, _018, _019, _020, _021, _022, _023, _024, _025, _026, _027, _028, _029, _030, _031, _032, _033, _034, _035, _036, _037, _038, _039, _040, _041, _042, _043, _044, _045, _046, _047, _048, _049, _050, _051, _052, _053, _054, _055, _056, _057, _058, _059, _060, _061, _062, _063, _064, _065, _066, _067, _068, _069, _070, _071, _072, _073, _074, _075, _076, _077, _078, _079, _080, _081, _082, _083, _084, _085, _086, _087, _088, _089, _090, _091, _092, _093, _094, _095, _096, _097, _098, _099, _100, _101, _102, _103, _104, _105, _106, _107, _108, _109, _110, _111, _112, _113, _114, _115, _116, _117, _118, _119, _120, _121, _122, _123, _124, _125, _126, _127, _128, _129, _130, _131, _132, _133, _134, _135, _136, _137, _138, _139, _140, _141, _142, _143, _144, _145, _146, _147, _148, _149, _150, _151, _152, _153, _154, _155, _156, _157, _158, _159, _160, _161, _162, _163, _164, _165, _166, _167, _168, _169, _170, _171, _172, _173, _174, _175, _176, _177, _178, _179, _180, _181, _182, _183, _184, _185, _186, _187, _188, _189, _190, _191, _192, _193, _194, _195, _196, _197, _198, _199
	}

	enum S {
		A, B, C
	}

	EnumSet<S> s;

	EnumSet<L> l;

	EnumSet<? extends Enum<?>> w;

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
		EnumSetBean other = (EnumSetBean) obj;
		if (l == null) {
			if (other.l != null) {
				return false;
			}
		} else if (!l.equals(other.l)) {
			return false;
		}
		if (s == null) {
			if (other.s != null) {
				return false;
			}
		} else if (!s.equals(other.s)) {
			return false;
		}
		if (w == null) {
			if (other.w != null) {
				return false;
			}
		} else if (!w.equals(other.w)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((l == null) ? 0 : l.hashCode());
		result = prime * result + ((s == null) ? 0 : s.hashCode());
		result = prime * result + ((w == null) ? 0 : w.hashCode());
		return result;
	}

}
