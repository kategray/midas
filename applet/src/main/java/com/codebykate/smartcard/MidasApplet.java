package com.codebykate.smartcard;

import javacard.framework.*;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;
import javacard.security.Signature;

public class MidasApplet extends Applet implements MultiSelectable
{
	private static final short BUFFER_SIZE = 32;

	/*
	 * Instructions
	 */
	private static final byte INS_GET_RANDOM = (short)0x10;
	private static final byte INS_SET_DIVERSIFICATION_KEY = (short)0x20;
	private static final byte INS_GET_DIVERSIFIED_KEY = (short)0x21;

	/*
	 * Constants
	 */
	public static final boolean NO_KEY_ENCRYPTION = false;
	public static final byte NO_OFFSET = 0x00;

	/* Do not permit key diversification with UIDs smaller than this. */
	private static final byte DIVERSIFICATION_MIN_LENGTH = 4;

	private byte[] tmpBuffer = null;
	private RandomData random = null;

	/*
	 * Card keys
	 */
	private GPKey diversifyKey = null;

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new MidasApplet(bArray, bOffset, bLength);
	}
	
	public MidasApplet(byte[] buffer, short offset, byte length)
	{
		// AES-128 Test Key
		byte[] testKey = {
			0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
			0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
		};

		diversifyKey = new GPKey (KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128);

		diversifyKey.setKey (testKey, GPKey.KEY_SOURCE_DEFAULT);
		register();
	}

	public void process(APDU apdu)
	{
		byte[] apduBuffer = apdu.getBuffer();
		byte apClass = apduBuffer[ISO7816.OFFSET_CLA];
		byte apInstruction = apduBuffer[ISO7816.OFFSET_INS];
		byte apLength = apduBuffer[ISO7816.OFFSET_LC];
		byte apParam1 = apduBuffer[ISO7816.OFFSET_P1];
		byte apParam2 = apduBuffer[ISO7816.OFFSET_P2];

		// Length of received packet
		short recvLength;

		if(selectingApplet()) {
			apdu.setOutgoingAndSend((short)0, (short)0);
			return;
		}

		switch(apInstruction & 0x00ff) {
			/*
			 * Class: 0x00|0x80
			 * Instruction: 0x10
			 * Length: 0|30
			 */
			case INS_GET_RANDOM:
				if ((apLength != 0x00) && apLength != BUFFER_SIZE) {
					ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
				}
				random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
				tmpBuffer = JCSystem.makeTransientByteArray(BUFFER_SIZE, JCSystem.CLEAR_ON_DESELECT);
				random.generateData(tmpBuffer, (short)0, BUFFER_SIZE);
				Util.arrayCopyNonAtomic(tmpBuffer, (short)0, apduBuffer, (short)0, BUFFER_SIZE);
				apdu.setOutgoingAndSend((short)0, BUFFER_SIZE);
				return;

			case INS_GET_DIVERSIFIED_KEY:
				// Ensure that the incoming data was received properly
				recvLength = apdu.setIncomingAndReceive();
				if(recvLength != apdu.getIncomingLength()) {
					ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
				}

				/*
					Diversified keys must have an input of at least DIVERSIFICATION_MIN_LENGTH.
					Default keys are exempt from this restriction for ease of testing.
				 */
				if ((diversifyKey.getKeySource() != GPKey.KEY_SOURCE_DEFAULT) && (recvLength < DIVERSIFICATION_MIN_LENGTH)) {
					ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
				}

				Signature cmac = new AESCMAC128();
				short signatureLength = diversifyKey.sign(cmac, apduBuffer, recvLength);
				apdu.setOutgoingAndSend (ISO7816.OFFSET_CDATA, signatureLength);

				break;

			case INS_SET_DIVERSIFICATION_KEY:
				ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
				break;

			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}

	}

	public boolean select(boolean b) {
		return true;
	}

	public void deselect(boolean b) {

	}
}
