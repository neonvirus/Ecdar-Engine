package features;

import logic.*;
import models.Automaton;
import org.junit.BeforeClass;
import org.junit.Test;
import parser.JSONParser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UniversityTest {

    private static TransitionSystem adm, admCopy, machine, machineCopy, researcher, researcherCopy, spec, specCopy,
            machine3, machine3Copy, adm2, adm2Copy, half1, half1Copy, half2, half2Copy;

    @BeforeClass
    public static void setUpBeforeClass() {
        String base = "./samples/json/EcdarUniversity/";
        String[] components = new String[]{"GlobalDeclarations.json",
                "Components/Administration.json",
                "Components/Machine.json",
                "Components/Researcher.json",
                "Components/Spec.json",
                "Components/Machine3.json",
                "Components/Adm2.json",
                "Components/HalfAdm1.json",
                "Components/HalfAdm2.json"};
        Automaton[] machines = JSONParser.parse(base, components, true);

        adm = new SimpleTransitionSystem(machines[0]);
        admCopy = new SimpleTransitionSystem(new Automaton(machines[0]));
        machine = new SimpleTransitionSystem(machines[1]);
        machineCopy = new SimpleTransitionSystem(new Automaton(machines[1]));
        researcher = new SimpleTransitionSystem(machines[2]);
        researcherCopy = new SimpleTransitionSystem(new Automaton(machines[2]));
        spec = new SimpleTransitionSystem(machines[3]);
        specCopy = new SimpleTransitionSystem(new Automaton(machines[3]));
        machine3 = new SimpleTransitionSystem(machines[4]);
        machine3Copy = new SimpleTransitionSystem(new Automaton(machines[4]));
        adm2 = new SimpleTransitionSystem(machines[5]);
        adm2Copy = new SimpleTransitionSystem(new Automaton(machines[5]));
        half1 = new SimpleTransitionSystem(machines[6]);
        half1Copy = new SimpleTransitionSystem(new Automaton(machines[6]));
        half2 = new SimpleTransitionSystem(machines[7]);
        half2Copy = new SimpleTransitionSystem(new Automaton(machines[7]));
    }

    @Test
    public void testAdm2RefinesSelf() {
        assertTrue(new Refinement(adm2, adm2Copy).check());
    }

    @Test
    public void testHalf1RefinesSelf() {
        assertTrue(new Refinement(half1, half1Copy).check());
    }

    @Test
    public void testHalf2RefinesSelf() {
        assertTrue(new Refinement(half2, half2Copy).check());
    }

    @Test
    public void testAdmRefinesSelf() {
        assertTrue(new Refinement(adm, admCopy).check());
    }

    @Test
    public void testMachineRefinesSelf() {
        assertTrue(new Refinement(machine, machineCopy).check());
    }

    @Test
    public void testMachineRefinesSelfDuplicate() {
        Refinement ref = new Refinement(machine, machine);
        assertFalse(ref.check());
        assert ref.getErrMsg().contains("Duplicate process instance");
    }

    @Test
    public void testResRefinesSelf() {
        assertTrue(new Refinement(researcher, researcherCopy).check());
    }

    @Test
    public void testSpecRefinesSelf() {
        assertTrue(new Refinement(spec, specCopy).check());
    }

    @Test
    public void testMachine3RefinesSelf() {
        assertTrue(new Refinement(machine3, machine3Copy).check());
    }

    @Test
    public void testAdmNotRefinesMachine() {
        assertFalse(new Refinement(adm, machine).check());
    }

    @Test
    public void testAdmNotRefinesResearcher() {
        assertFalse(new Refinement(adm, researcher).check());
    }

    @Test
    public void testAdmNotRefinesSpec() {
        assertFalse(new Refinement(adm, spec).check());
    }

    @Test
    public void testAdmNotRefinesMachine3() {
        assertFalse(new Refinement(adm, machine3).check());
    }

    @Test
    public void testMachineNotRefinesAdm() {
        assertFalse(new Refinement(machine, adm).check());
    }

    @Test
    public void testMachineNotRefinesResearcher() {
        assertFalse(new Refinement(machine, researcher).check());
    }

    @Test
    public void testMachineNotRefinesSpec() {
        assertFalse(new Refinement(machine, spec).check());
    }

    @Test
    public void testMachineNotRefinesMachine3() {
        assertFalse(new Refinement(machine, machine3).check());
    }

    @Test
    public void testResNotRefinesAdm() {
        assertFalse(new Refinement(researcher, adm).check());
    }

    @Test
    public void testResNotRefinesMachine() {
        assertFalse(new Refinement(researcher, machine).check());
    }

    @Test
    public void testResNotRefinesSpec() {
        assertFalse(new Refinement(researcher, spec).check());
    }

    @Test
    public void testResNotRefinesMachine3() {
        assertFalse(new Refinement(researcher, machine3).check());
    }

    @Test
    public void testSpecNotRefinesAdm() {
        assertFalse(new Refinement(spec, adm).check());
    }

    @Test
    public void testSpecNotRefinesMachine() {
        assertFalse(new Refinement(spec, machine).check());
    }

    @Test
    public void testSpecNotRefinesResearcher() {
        assertFalse(new Refinement(spec, researcher).check());
    }

    @Test
    public void testSpecNotRefinesMachine3() {
        assertFalse(new Refinement(spec, machine3).check());
    }

    @Test
    public void testMachine3RefinesMachine() {
        assertTrue(new Refinement(machine3, machine).check());
    }

    @Test
    public void testMachine3NotRefinesAdm() {
        assertFalse(new Refinement(machine3, adm).check());
    }

    @Test
    public void testMachine3NotRefinesResearcher() {
        assertFalse(new Refinement(machine3, researcher).check());
    }

    @Test
    public void testMachine3NotRefinesSpec() {
        assertFalse(new Refinement(machine3, spec).check());
    }

    @Test
    public void testCompRefinesSpec() {
        assertTrue(new Refinement(new Composition(new TransitionSystem[]{adm, machine, researcher}), spec).check());
    }

    @Test
    public void testCompOfCompRefinesSpec() {
        assertTrue(new Refinement(
                new Composition(new TransitionSystem[]{adm,
                        new Composition(new TransitionSystem[]{machine, researcher})}),
                spec).check()
        );
    }

    @Test
    public void testCompRefinesSelf() {
        Refinement ref = new Refinement(
                new Composition(new TransitionSystem[]{adm, machine, researcher}),
                new Composition(new TransitionSystem[]{machineCopy, researcherCopy, admCopy}));
        assertTrue(ref.check());
    }

    @Test
    public void testCompRefinesSelfDuplicate() {
        Refinement ref = new Refinement(
                new Composition(new TransitionSystem[]{adm, machine, researcher}),
                new Composition(new TransitionSystem[]{machine, researcher, adm}));

        assertFalse(ref.check(true));
        assert ref.getErrMsg().contains("Duplicate process instance");
    }

    @Test
    public void testUncomposable() {
        boolean fail = false;

        try {
            new Refinement(
                    new Composition(new TransitionSystem[]{machine, machine3}),
                    machine);
        } catch (IllegalArgumentException ex) {
            fail = true;
        }

        assertTrue(fail);
    }

    @Test
    public void testHalf1AndHalf2RefinesAdm2() {
        assertTrue(new Refinement(new Conjunction(new TransitionSystem[]{half1, half2}), adm2).check());
    }

    @Test
    public void testAdm2RefinesHalf1AndHalf2() {
        assertTrue(new Refinement(adm2, new Conjunction(new TransitionSystem[]{half1, half2})).check());
    }
}