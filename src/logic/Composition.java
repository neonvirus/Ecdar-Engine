package logic;

import lib.DBMLib;
import models.*;

import java.io.File;
import java.util.*;

public class Composition {

		public static void compose(ArrayList<Component> machines) {
				// load library
				String fileName = "src/" + System.mapLibraryName("DBM");
				File lib = new File(fileName);
				System.load(lib.getAbsolutePath());

				State initialState = computeInitial(machines);

				ArrayList<State> passed = new ArrayList<>();
				Deque<State> waiting = new ArrayDeque<>();
				ArrayList<Clock> clocks = getClocks(machines);
				ArrayList<StateTransition> stateTransitions = new ArrayList<>();
				waiting.push(initialState);

				Set<Channel> inputsOutside = new HashSet<>();
				Set<Channel> outputsOutside = new HashSet<>();
				Set<Channel> syncs = new HashSet<>();

				int machineCount = machines.size();

				for (int i = 0; i < machineCount; i++) {
						Set<Channel> inputsOfI = new HashSet<>();
						inputsOfI.addAll(machines.get(i).getInputAct());
						Set<Channel> outputsOfI = new HashSet<>();
						outputsOfI.addAll(machines.get(i).getOutputAct());
						Set<Channel> sync = new HashSet<>();
						sync.addAll(machines.get(i).getOutputAct());

						Set<Channel> outputsOfOthers = new HashSet<>();
						Set<Channel> inputsOfOthers = new HashSet<>();

						for (int j = 0; j < machineCount; j++) {
								if (i != j) {
										outputsOfOthers.addAll(machines.get(j).getOutputAct());

										inputsOfOthers.addAll(machines.get(j).getInputAct());

										sync.retainAll(machines.get(j).getInputAct());
										syncs.addAll(sync);
								}
						}

						// set difference
						inputsOfI.removeAll(outputsOfOthers);
						outputsOfI.removeAll(inputsOfOthers);

						inputsOutside.addAll(inputsOfI);
						outputsOutside.addAll(outputsOfI);
				}

				while (waiting.size() > 0) {
						// get state from top of stack
						State state = waiting.pop();
						// delay
						int[] dbm = delay(state.getZone());
						// apply invariants
						dbm = applyInvariantsOrGuards(dbm, clocks, getInvariants(state.getLocations()));
						state.setZone(dbm);

						if (!passedContainsState(passed, state)) {
								passed.add(state);
								ArrayList<Location> locations = state.getLocations();
								for (int i = 0; i < locations.size(); i++) {
										Component component = machines.get(i);
										Location loc = locations.get(i);
										ArrayList<Transition> transitions = component.getTransitionsFromLocation(loc);
										for (Transition transition : transitions) {
												Channel channel = transition.getChannel();
												if (inputsOutside.contains(channel) || outputsOutside.contains(channel)) {
														// build state
														ArrayList<Location> newLocations = new ArrayList<>();
														newLocations.addAll(locations);
														newLocations.set(i, transition.getTo());
														ArrayList<Guard> guards = transition.getGuards();
														ArrayList<Update> updates = transition.getUpdates();
														// apply guards
														dbm = applyInvariantsOrGuards(dbm, clocks, guards);
														// apply resets
														dbm = applyResets(dbm, clocks, updates);
														State newState = new State(newLocations, dbm);
														if (!waiting.contains(newState) || !passedContainsState(passed, newState)) {
																waiting.push(newState);
														}
														boolean isInput = inputsOutside.contains(channel);
														StateTransition stateTransition = new StateTransition(state, newState, channel, isInput, guards, updates);
														if (!stateTransitionsContainsTransition(stateTransitions, stateTransition))
																stateTransitions.add(stateTransition);
												}
												if (syncs.contains(channel)) {
														for (int j = 0; j < locations.size(); j++) {
																if (i != j) {
																		Location locJ = locations.get(j);
																		Component machine = machines.get(j);
																		ArrayList<Transition> transitionsJ = machine.getTransitionsFromLocation(locJ);
																		transitionsJ.removeIf(n -> n.getChannel() != channel);
																		for (Transition transitionJ : transitionsJ) {
																				// build state
																				ArrayList<Location> newLocations = new ArrayList<>();
																				newLocations.addAll(locations);
																				newLocations.set(i, transition.getTo());
																				newLocations.set(j, transitionJ.getTo());

																				ArrayList<Guard> guards = new ArrayList<>();
																				guards.addAll(transition.getGuards());
																				guards.addAll(transitionJ.getGuards());

																				ArrayList<Update> updates = new ArrayList<>();
																				updates.addAll(transition.getUpdates());
																				updates.addAll(transitionJ.getUpdates());

																				// apply guards
																				dbm = applyInvariantsOrGuards(dbm, clocks, guards);
																				// apply resets
																				dbm = applyResets(dbm, clocks, updates);
																				State newState = new State(newLocations, dbm);
																				if (!waiting.contains(newState) || !passedContainsState(passed, newState)) {
																						waiting.push(newState);
																				}
																				StateTransition stateTransition = new StateTransition(state, newState, channel, true, guards, updates);
																				if (!stateTransitionsContainsTransition(stateTransitions, stateTransition))
																						stateTransitions.add(stateTransition);
																		}
																}
														}
												}
										}
								}
						}
				}
		}

		private static State computeInitial(ArrayList<Component> machines) {
				ArrayList<Location> initialLocations = new ArrayList<>();
				ArrayList<Clock> allClocks = new ArrayList<>();
				ArrayList<Guard> invariants = new ArrayList<>();
				for (Component machine : machines) {
						Location init = machine.getInitLoc();
						initialLocations.add(init);
						Set<Clock> clocks = machine.getClocks();
						for (Clock clock : clocks) {
								clock.setValue(0);
								allClocks.add(clock);
						}
						if (init.getInvariant() != null) invariants.add(init.getInvariant());
				}
				int[] zone = initializeDBM(allClocks);
				return new State(initialLocations, zone);
		}

		private static boolean passedContainsState(ArrayList<State> passed, State state) {
				// keep only states that have the same locations
				ArrayList<State> passedCopy = new ArrayList<>();
				passedCopy.addAll(passed);
				passedCopy.removeIf(n -> !Arrays.equals(n.getLocations().toArray(), state.getLocations().toArray()));

				for (State passedState : passedCopy) {
						int dim = (int) Math.sqrt(passedState.getZone().length);
						// check for zone inclusion
						if (DBMLib.dbm_isSubsetEq(state.getZone(), passedState.getZone(), dim)) {
								return true;
						}
				}
				return false;
		}

		private static boolean stateTransitionsContainsTransition(ArrayList<StateTransition> transitions, StateTransition transition) {
				for (StateTransition stateTransition : transitions) {
						if (stateTransition.equals(transition))
								return true;
				}
				return false;
		}

		private static ArrayList<Clock> getClocks(ArrayList<Component> components) {
				ArrayList<Clock> allClocks = new ArrayList<>();

				for (Component component : components) {
						Set<Clock> clocks = component.getClocks();
						allClocks.addAll(clocks);
				}

				return allClocks;
		}

		private static ArrayList<Guard> getInvariants(ArrayList<Location> locations) {
				ArrayList<Guard> invariants = new ArrayList<>();

				for (Location location : locations) {
						Guard invariant = location.getInvariant();
						if (invariant != null) invariants.add(invariant);
				}

				return invariants;
		}

		private static int[] initializeDBM(ArrayList<Clock> clocks) {
				// we need a DBM of size n*n, where n is the number of clocks (x0, x1, x2, ... , xn)
				// clocks x1 to xn are clocks derived from our components, while x0 is a reference clock needed by the library
				int size = clocks.size() + 1;
				// initially dbm is an array of 0's, which is what we need
				return new int[size*size];
		}

		private static int[] delay(int[] dbm) {
				return DBMLib.dbm_up(dbm, (int) Math.sqrt(dbm.length));
		}

		private static int[] applyInvariantsOrGuards(int[] dbm, ArrayList<Clock> clocks, ArrayList<Guard> guards) {
				int size = (int) Math.sqrt(dbm.length);

				// take 2 guards at a time in order to determine constraint (x-y and y-x)
				for (int i = 0; i < guards.size(); i++) {
						for (int j = (i + 1); j < guards.size(); j++) {
								// get guard and then its index in the clock array so you know the index in the DBM
								Guard g1 = guards.get(i); int a = clocks.indexOf(g1.getClock());
								Guard g2 = guards.get(j); int b = clocks.indexOf(g2.getClock());

								// add constraints to dbm
								dbm = buildConstraint(dbm, size, (a+1), (b+1), g1, g2);
								dbm = buildConstraint(dbm, size, (b+1), (a+1), g2, g1);

						}
				}
				return dbm;
		}

		private static int[] applyResets(int[] dbm, ArrayList<Clock> clocks, ArrayList<Update> resets) {
				int size = (int) Math.sqrt(dbm.length);

				for (Update reset : resets) {
						int index = clocks.indexOf(reset.getClock());

						dbm = DBMLib.dbm_updateValue(dbm, size, (index+1), reset.getValue());
				}

				return dbm;
		}

		private static int[] buildConstraint(int[] dbm, int size, int i, int j, Guard g1, Guard g2) {
				// determine constraint between 2 guards on clocks x and y by taking x's upper bound - y's lower bound
				int bound = (g1.upperBound() == Integer.MAX_VALUE) ? Integer.MAX_VALUE : g1.upperBound() - g2.lowerBound();
				// if either guard is strict, the constraint is also strict
				boolean strict = (g1.isStrict() || g2.isStrict());

				return DBMLib.dbm_constrain1(dbm, size, i, j, bound, strict);
		}

		private static void printDBM(int[] dbm) {
				for (int x : dbm) System.out.print(x + "  ");
				System.out.println();
		}
}
