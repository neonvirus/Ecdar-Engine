package models;

public class Location {

		private String name;
		private Guard invariant;
		private boolean isInitial;
		private boolean isUrgent;
		private boolean isUniversal;
		private boolean isInconsistent;
		public Location next;

		public Location(String name, Guard invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent) {
				this.name = name;
				this.invariant = invariant;
				this.isInitial = isInitial;
				this.isUrgent = isUrgent;
				this.isUniversal = isUniversal;
				this.isInconsistent = isInconsistent;
				this.next = null;
		}

		public Location(String name, Guard invariant, boolean isInitial, boolean isUrgent, boolean isUniversal, boolean isInconsistent, Location next) {
				this.name = name;
				this.invariant = invariant;
				this.isInitial = isInitial;
				this.isUrgent = isUrgent;
				this.isUniversal = isUniversal;
				this.isInconsistent = isInconsistent;
				this.next = next;
		}

		public String getName() { return name; }

		public void setName(String name) { this.name = name;}

		public Guard getInvariant() {
				return invariant;
		}

		public void setInvariant(Guard invariant) {
				this.invariant = invariant;
		}

		public boolean isInitial() {
				return isInitial;
		}

		public void setInitial(boolean initial) {
				isInitial = initial;
		}

		public boolean isUrgent() {
				return isUrgent;
		}

		public void setUrgent(boolean urgent) {
				isUrgent = urgent;
		}

		public boolean isUniversal() {
				return isUniversal;
		}

		public void setUniversal(boolean universal) {
				isUniversal = universal;
		}

		public boolean isInconsistent() {
				return isInconsistent;
		}

		public void setInconsistent(boolean inconsistent) {
				isInconsistent = inconsistent;
		}

		public Location getNext() { return this.next; }

		@Override
		public String toString() {
				String str = "Location (" + this.name;

				Location nxt = this.next;
				while (nxt != null) {
						str += (", " + next.getName());
						nxt = nxt.next;
				}

				str += ")";
				return str;
		}
}