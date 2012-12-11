package org.drools.integrationtests;

import org.drools.CommonTestMethodBase;
import org.drools.Message;
import org.junit.Test;
import org.kie.builder.GAV;
import org.kie.builder.KieBaseModel;
import org.kie.builder.KieBuilder;
import org.kie.builder.KieFactory;
import org.kie.builder.KieFileSystem;
import org.kie.builder.KieModuleModel;
import org.kie.builder.KieServices;
import org.kie.builder.KieSessionModel;
import org.kie.builder.KieSessionModel.KieSessionType;
import org.kie.conf.AssertBehaviorOption;
import org.kie.conf.EventProcessingOption;
import org.kie.runtime.KieSession;
import org.kie.runtime.conf.ClockTypeOption;

/**
 * This is a sample class to launch a rule.
 */
public class KieHelloWorldTest extends CommonTestMethodBase {

    @Test
    public void testHelloWorld() throws Exception {
        String drl = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";
        
        KieServices ks = KieServices.Factory.get();
        KieFactory kf = KieFactory.Factory.get();
        
        KieFileSystem kfs = kf.newKieFileSystem().write( "src/main/resources/r1.drl", drl );
        ks.newKieBuilder( kfs ).buildAll();

        KieSession ksession = ks.getKieContainer(ks.getKieRepository().getDefaultGAV()).getKieSession();
        ksession.insert(new Message("Hello World"));

        int count = ksession.fireAllRules();
         
        assertEquals( 1, count );
    }

    @Test
    public void testFailingHelloWorld() throws Exception {
        String drl = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message( mesage == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();
        KieFactory kf = KieFactory.Factory.get();

        KieFileSystem kfs = kf.newKieFileSystem().write( "src/main/resources/r1.drl", drl );
        
        KieBuilder kb = ks.newKieBuilder( kfs ).buildAll();

        assertEquals( 1, kb.getResults().getMessages().size() );
    }

    @Test
    public void testHelloWorldWithPackages() throws Exception {
        String drl1 = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        String drl2 = "package org.drools\n" +
                "rule R2 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();
        KieFactory kf = KieFactory.Factory.get();

        GAV gav = kf.newGav("org.kie", "hello-world", "1.0-SNAPSHOT");

        KieFileSystem kfs = kf.newKieFileSystem()
                .generateAndWritePomXML( gav )
                .write("src/main/resources/KBase1/org/pkg1/r1.drl", drl1)
                .write("src/main/resources/KBase1/org/pkg2/r2.drl", drl2)
                .writeKModuleXML(createKieProjectWithPackages(kf, "org.pkg1").toXML());
        ks.newKieBuilder( kfs ).buildAll();

        KieSession ksession = ks.getKieContainer(gav).getKieSession("KSession1");
        ksession.insert(new Message("Hello World"));

        int count = ksession.fireAllRules();

        assertEquals( 1, count );
    }

    @Test
    public void testHelloWorldWithWildcardPackages() throws Exception {
        String drl1 = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        String drl2 = "package org.drools\n" +
                "rule R2 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();
        KieFactory kf = KieFactory.Factory.get();

        GAV gav = kf.newGav("org.kie", "hello-world", "1.0-SNAPSHOT");

        KieFileSystem kfs = kf.newKieFileSystem()
                .generateAndWritePomXML( gav )
                .write("src/main/resources/KBase1/org/pkg1/test/r1.drl", drl1)
                .write("src/main/resources/KBase1/org/pkg2/test/r2.drl", drl2)
                .writeKModuleXML( createKieProjectWithPackages(kf, "org.pkg1.*").toXML());
        ks.newKieBuilder( kfs ).buildAll();

        KieSession ksession = ks.getKieContainer(gav).getKieSession("KSession1");
        ksession.insert(new Message("Hello World"));

        int count = ksession.fireAllRules();

        assertEquals( 1, count );
    }

    private KieModuleModel createKieProjectWithPackages(KieFactory kf, String pkg) {
        KieModuleModel kproj = kf.newKieModuleModel();

        KieBaseModel kieBaseModel1 = kproj.newKieBaseModel("KBase1")
                .setEqualsBehavior( AssertBehaviorOption.EQUALITY )
                .setEventProcessingMode( EventProcessingOption.STREAM )
                .addPackage(pkg);

        KieSessionModel ksession1 = kieBaseModel1.newKieSessionModel("KSession1")
                .setType( KieSessionType.STATEFUL )
                .setClockType(ClockTypeOption.get("realtime"));

        return kproj;
    }

    @Test
    public void testHelloWorldOnVersionRange() throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieFactory kf = KieFactory.Factory.get();

        buildVersion(ks, kf, "Hello World", "1.0");
        buildVersion(ks, kf, "Aloha Earth", "1.1");
        buildVersion(ks, kf, "Hi Universe", "1.2");

        GAV latestGav = kf.newGav("org.kie", "hello-world", "LATEST");

        KieSession ksession = ks.getKieContainer(latestGav).getKieSession("KSession1");
        ksession.insert(new Message("Hello World"));
        assertEquals( 0, ksession.fireAllRules() );

        ksession = ks.getKieContainer(latestGav).getKieSession("KSession1");
        ksession.insert(new Message("Hi Universe"));
        assertEquals( 1, ksession.fireAllRules() );

        GAV gav1 = kf.newGav("org.kie", "hello-world", "1.0");

        ksession = ks.getKieContainer(gav1).getKieSession("KSession1");
        ksession.insert(new Message("Hello World"));
        assertEquals( 1, ksession.fireAllRules() );

        ksession = ks.getKieContainer(gav1).getKieSession("KSession1");
        ksession.insert(new Message("Hi Universe"));
        assertEquals( 0, ksession.fireAllRules() );

        GAV gav2 = kf.newGav("org.kie", "hello-world", "[1.0,1.2)");

        ksession = ks.getKieContainer(gav2).getKieSession("KSession1");
        ksession.insert(new Message("Aloha Earth"));
        assertEquals( 1, ksession.fireAllRules() );

        ksession = ks.getKieContainer(gav2).getKieSession("KSession1");
        ksession.insert(new Message("Hi Universe"));
        assertEquals( 0, ksession.fireAllRules() );
    }

    private void buildVersion(KieServices ks, KieFactory kf, String message, String version) {
        String drl = "package org.drools\n" +
                "rule R1 when\n" +
                "   $m : Message( message == \"" + message+ "\" )\n" +
                "then\n" +
                "end\n";

        GAV gav = kf.newGav("org.kie", "hello-world", version);

        KieFileSystem kfs = kf.newKieFileSystem()
                .generateAndWritePomXML( gav )
                .write("src/main/resources/KBase1/org/pkg1/r1.drl", drl)
                .writeKModuleXML(createKieProjectWithPackages(kf, "*").toXML());
        ks.newKieBuilder( kfs ).buildAll();
    }

    @Test
    public void testHelloWorldWithPackagesAnd2KieBases() throws Exception {
        String drl1 = "package org.drools\n" +
                "rule R11 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n" +
                "rule R12 when\n" +
                "   $m : Message( message == \"Hi Universe\" )\n" +
                "then\n" +
                "end\n";

        String drl2 = "package org.drools\n" +
                "rule R21 when\n" +
                "   $m : Message( message == \"Hello World\" )\n" +
                "then\n" +
                "end\n" +
                "rule R22 when\n" +
                "   $m : Message( message == \"Aloha Earth\" )\n" +
                "then\n" +
                "end\n";

        KieServices ks = KieServices.Factory.get();
        KieFactory kf = KieFactory.Factory.get();

        GAV gav = kf.newGav("org.kie", "hello-world", "1.0-SNAPSHOT");

        KieFileSystem kfs = kf.newKieFileSystem()
                .generateAndWritePomXML( gav )
                .write("src/main/resources/KBase1/org/pkg1/r1.drl", drl1)
                .write("src/main/resources/KBase1/org/pkg2/r2.drl", drl2)
                .writeKModuleXML(createKieProjectWithPackagesAnd2KieBases(kf).toXML());
        ks.newKieBuilder( kfs ).buildAll();

        KieSession ksession = ks.getKieContainer(gav).getKieSession("KSession1");
        ksession.insert(new Message("Hello World"));
        assertEquals( 1, ksession.fireAllRules() );

        ksession = ks.getKieContainer(gav).getKieSession("KSession1");
        ksession.insert(new Message("Hi Universe"));
        assertEquals( 1, ksession.fireAllRules() );

        ksession = ks.getKieContainer(gav).getKieSession("KSession1");
        ksession.insert(new Message("Aloha Earth"));
        assertEquals( 0, ksession.fireAllRules() );

        ksession = ks.getKieContainer(gav).getKieSession("KSession2");
        ksession.insert(new Message("Hello World"));
        assertEquals( 1, ksession.fireAllRules() );

        ksession = ks.getKieContainer(gav).getKieSession("KSession2");
        ksession.insert(new Message("Hi Universe"));
        assertEquals( 0, ksession.fireAllRules() );

        ksession = ks.getKieContainer(gav).getKieSession("KSession2");
        ksession.insert(new Message("Aloha Earth"));
        assertEquals( 1, ksession.fireAllRules() );
    }

    private KieModuleModel createKieProjectWithPackagesAnd2KieBases(KieFactory kf) {
        KieModuleModel kproj = kf.newKieModuleModel();

        kproj.newKieBaseModel("KBase2")
                .setEqualsBehavior( AssertBehaviorOption.EQUALITY )
                .setEventProcessingMode( EventProcessingOption.STREAM )
                .addPackage("org.pkg1")
                .newKieSessionModel("KSession1");

        kproj.newKieBaseModel("KBase1")
                .setEqualsBehavior( AssertBehaviorOption.EQUALITY )
                .setEventProcessingMode( EventProcessingOption.STREAM )
                .addPackage("org.pkg2")
                .newKieSessionModel("KSession2");

        return kproj;
    }
}