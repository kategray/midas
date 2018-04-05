package tests;

import cardTools.CardManager;
import cardTools.RunConfig;
import cardTools.Util;
import com.codebykate.smartcard.MidasApplet;
import org.junit.Assert;
import org.testng.annotations.*;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.*;

/**
 * Test applet functionality
 *
 * @author Kate Gray (opensource@codebykate.com)
 */
public class AppletTest {
    private static String APPLET_AID                  = "f0 44 69 6e 65 72 6f 00";
    private static byte APPLET_AID_BYTE[]             = Util.hexStringToByteArray(APPLET_AID);

    private static final String APDU_GET_RANDOM       = "80 10  00 00  00";
    private static final String APDU_DIVERSIFY_EMPTY  = "80 21  00 00  00";
    private static final String APDU_DIVERSIFY_LEN16  = "80 21  00 00  10" +
                                                        "6b c1 be e2 2e 40 9f 96 e9 3d 7e 11 73 93 17 2a  00";
    private static final String APDU_DIVERSIFY_LEN40  = "80 21  00 00  28" +
                                                        "6b c1 be e2 2e 40 9f 96 e9 3d 7e 11 73 93 17 2a" +
                                                        "ae 2d 8a 57 1e 03 ac 9c 9e b7 6f ac 45 af 8e 51" +
                                                        "30 c8 1c 46 a3 5c e4 11  00";
    private static final String APDU_DIVERSIFY_LEN64  = "80 21 00 00 40 6b c1 be e2 2e 40 9f 96 e9 3d 7e" +
                                                        "11 73 93 17 2a ae 2d 8a 57 1e 03 ac 9c 9e b7 6f" +
                                                        "ac 45 af 8e 51 30 c8 1c 46 a3 5c e4 11 e5 fb c1" +
                                                        "19 1a 0a 52 ef f6 9f 24 45 df 4f 9b 17 ad 2b 41" +
                                                        "7b e6 6c 37 10 00";
    static CardManager cardManager;
    static RunConfig runConfig;
    
    public AppletTest() {
    }

    public static ResponseAPDU sendCommandWithInitSequence(CardManager cardMngr, String command, ArrayList<String> initCommands) throws CardException {
        if (initCommands != null) {
            for (String cmd : initCommands) {
                cardMngr.getChannel().transmit(new CommandAPDU(Util.hexStringToByteArray(cmd)));
            }
        }

        final ResponseAPDU resp = cardMngr.getChannel().transmit(new CommandAPDU(Util.hexStringToByteArray(command)));
        return resp;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        cardManager = new CardManager(true, APPLET_AID_BYTE);
        runConfig = RunConfig.getDefaultConfig();

        // Running on physical card
        //runCfg.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL);

        // Running in the simulator
        runConfig.setAppletToSimulate(MidasApplet.class)
                .setTestCardType(RunConfig.CARD_TYPE.JCARDSIMLOCAL)
                .setbReuploadApplet(true)
                .setInstallData(new byte[8]);

        System.out.print("Connecting to card...");
        if (!cardManager.Connect(runConfig)) {
            return;
        }
        System.out.println(" Done.");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    // Random number test
    @Test
    public void testGetRandom() throws Exception {
        final ResponseAPDU firstAPDU = sendCommandWithInitSequence(cardManager, APDU_GET_RANDOM, null);
        final ResponseAPDU secondAPDU = sendCommandWithInitSequence(cardManager, APDU_GET_RANDOM, null);

        // Check that each APDU returns successfully
        Assert.assertNotNull(firstAPDU);
        Assert.assertEquals(0x9000, firstAPDU.getSW());
        Assert.assertNotNull(firstAPDU.getData());

        Assert.assertNotNull(secondAPDU);
        Assert.assertEquals(0x9000, secondAPDU.getSW());
        Assert.assertNotNull(secondAPDU.getData());

        // Verify that the two calls get different results
        Assert.assertThat(firstAPDU.getData(), not(equalTo(secondAPDU.getData())));
    }

    // Diversification Test
    @Test
    public void testDiversify() throws Exception {
        byte[] diversifyEmptyCorrect = new byte[] {
            (byte)0x97, (byte)0xdd, (byte)0x6e, (byte)0x5a, (byte)0x88, (byte)0x2c, (byte)0xbd, (byte)0x56,
            (byte)0x4c, (byte)0x39, (byte)0xae, (byte)0x7d, (byte)0x1c, (byte)0x5a, (byte)0x31, (byte)0xaa
        };

        byte[] diversifyLen16Correct = new byte[]{
            (byte)0xd0, (byte)0xbc, (byte)0x5b, (byte)0xb4, (byte)0xd6, (byte)0xf6, (byte)0x0d, (byte)0x5b,
            (byte)0x17, (byte)0xb7, (byte)0xbf, (byte)0x79, (byte)0x4b, (byte)0x45, (byte)0x43, (byte)0x6d
        };

        byte[] diversifyLen40Correct = new byte[]{
            (byte)0x98, (byte)0x9b, (byte)0xaf, (byte)0xbf, (byte)0xce, (byte)0x64, (byte)0xb3, (byte)0x9b,
            (byte)0x28, (byte)0xed, (byte)0xf0, (byte)0x37, (byte)0x9e, (byte)0x6e, (byte)0xf5, (byte)0xdd
        };

        byte[] diversifyLen64Correct = new byte[]{
            (byte)0x58, (byte)0x27, (byte)0x9a, (byte)0x23, (byte)0x97, (byte)0xf2, (byte)0x32, (byte)0x98,
            (byte)0x9c, (byte)0x4c, (byte)0x28, (byte)0xc1, (byte)0xb1, (byte)0x71, (byte)0x09, (byte)0x79
        };

        final ResponseAPDU diversifyEmptyAPDU = sendCommandWithInitSequence(cardManager, APDU_DIVERSIFY_EMPTY, null);
        final ResponseAPDU diversifyLen16APDU = sendCommandWithInitSequence(cardManager, APDU_DIVERSIFY_LEN16, null);
        final ResponseAPDU diversifyLen40APDU = sendCommandWithInitSequence(cardManager, APDU_DIVERSIFY_LEN40, null);
        final ResponseAPDU diversifyLen64APDU = sendCommandWithInitSequence(cardManager, APDU_DIVERSIFY_LEN64, null);

        // Empty Diversify ID
        Assert.assertNotNull(diversifyEmptyAPDU);
        Assert.assertEquals(0x9000, diversifyEmptyAPDU.getSW());
        Assert.assertNotNull(diversifyEmptyAPDU.getData());
        Assert.assertThat(diversifyEmptyAPDU.getData(), equalTo(diversifyEmptyCorrect));

        // Length 16
        Assert.assertNotNull(diversifyLen16APDU);
        Assert.assertEquals(0x9000, diversifyLen16APDU.getSW());
        Assert.assertNotNull(diversifyLen16APDU.getData());
        Assert.assertThat(diversifyLen16APDU.getData(), equalTo(diversifyLen16Correct));

        // Length 40
        Assert.assertNotNull(diversifyLen40APDU);
        Assert.assertEquals(0x9000, diversifyLen40APDU.getSW());
        Assert.assertNotNull(diversifyLen40APDU.getData());
        Assert.assertThat(diversifyLen40APDU.getData(), equalTo(diversifyLen40Correct));

        // Length 64
        Assert.assertNotNull(diversifyLen64APDU);
        Assert.assertEquals(0x9000, diversifyLen64APDU.getSW());
        Assert.assertNotNull(diversifyLen64APDU.getData());
        Assert.assertThat(diversifyLen64APDU.getData(), equalTo(diversifyLen64Correct));
    }
}
