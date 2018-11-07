package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Component {

		private ArrayList<Location> locations;
		private ArrayList<Transition> transitions;
		private Set<Clock> clocks;
		private Set<Channel> inputAct;
		private Set<Channel> outputAct;
		private Location initLoc;

		public Component(ArrayList<Location> locations, ArrayList<Transition> transitions, Set<Clock> clocks) {
				this.locations = locations;
				for (Location location : locations) {
						if (location.isInitial()) {
								initLoc = location;
								break;
						}
				}
				this.inputAct = new HashSet<>();
				this.outputAct = new HashSet<>();
				setTransitions(transitions);
				this.clocks = clocks;
		}

		public ArrayList<Location> getLocations() {
				return locations;
		}

		public void setLocations(ArrayList<Location> locations) {
				this.locations = locations;
		}

		public ArrayList<Transition> getTransitions() {
				return transitions;
		}

		public ArrayList<Transition> getTransitionsFromLocation(Location loc) {
				ArrayList<Transition> trans = new ArrayList<>();

				trans.addAll(transitions);
				trans.removeIf(n -> n.getFrom() != loc);

				return trans;
		}

		public void setTransitions(ArrayList<Transition> transitions) {
				this.transitions = transitions;
				for (Transition transition : transitions) {
						Channel action = transition.getChannel();
						if (transition.isInput()) {
								inputAct.add(action);
						} else {
								outputAct.add(action);
						}
				}
		}

		public Set<Clock> getClocks() {
				return clocks;
		}

		public void setClocks(Set<Clock> clocks) {
				this.clocks = clocks;
		}

		public Location getInitLoc() {
				return initLoc;
		}

		public Set<Channel> getInputAct() {
				return inputAct;
		}

		public Set<Channel> getOutputAct() {
				return outputAct;
		}

		public Set<Channel> getActions() {
				Set<Channel> actions = new HashSet<>();

				actions.addAll(this.inputAct);
				actions.addAll(this.outputAct);

				return actions;
		}
}
