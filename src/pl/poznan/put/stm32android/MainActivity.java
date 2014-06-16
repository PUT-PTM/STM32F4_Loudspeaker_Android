package pl.poznan.put.stm32android;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pl.poznan.put.stm32android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String ACTION_USB_PERMISSION = "com.google.android.HID.action.USB_PERMISSION";
	private static final String MESSAGE_SELECT_YOUR_USB_HID_DEVICE = "Wybierz urz¹dzenie HID";
	private static final String MESSAGE_CONNECT_YOUR_USB_HID_DEVICE = "Pod³¹cz urz¹dzenie HID";
	private static final String RECEIVE_DATA_FORMAT = "receiveDataFormat";
	private static final String BINARY = "binary";
	private static final String INTEGER = "integer";
	private static final String HEXADECIMAL = "hexadecimal";
	private static final String TEXT = "text";
	private static final String DELIMITER = "delimiter";
	private static final String DELIMITER_NONE = "none";
	private static final String DELIMITER_NEW_LINE = "newLine";
	private static final String DELIMITER_SPACE = "space";
	private static final String NEW_LINE = "\n";
	private static final String SPACE = " ";

	private PendingIntent mPermissionIntent;

	private SharedPreferences sharedPreferences;

	private UsbDevice device;
	private UsbManager mUsbManager;

	private UsbInterface intf;
	private UsbEndpoint endPointRead;
	private UsbEndpoint endPointWrite;
	private UsbDeviceConnection connection;
	private int packetSize;

	private EditText log_txt;
	private EditText edtxtHidInput;
	private Button button1;
	private Button button2;
	private Button button3;
	private Button button4;
	private Button button5;
	private Button button6;
	private Button button7;
	private Button btnSelectHIDDevice;
	private Button btnClear;
	private RadioButton radioButton;
	private Timer myTimer = new Timer();
	private final Handler uiHandler = new Handler();
	private String settingsDelimiter;
	private String delimiter;

	private String receiveDataFormat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			setVersionToTitle();
			
			button1 = (Button) findViewById(R.id.button1);
			button1.setOnClickListener(this);
			button2 = (Button) findViewById(R.id.button2);
			button2.setOnClickListener(this);
			button3 = (Button) findViewById(R.id.button3);
			button3.setOnClickListener(this);
			button4 = (Button) findViewById(R.id.button4);
			button4.setOnClickListener(this);
			button5 = (Button) findViewById(R.id.button5);
			button5.setOnClickListener(this);
			button6 = (Button) findViewById(R.id.button6);
			button6.setOnClickListener(this);
			button7 = (Button) findViewById(R.id.button7);
			button7.setOnClickListener(this);

			btnSelectHIDDevice = (Button) findViewById(R.id.btnSelectHIDDevice);
			btnSelectHIDDevice.setOnClickListener(this);

			btnClear = (Button) findViewById(R.id.btnClear);
			btnClear.setOnClickListener(this);

			edtxtHidInput = (EditText) findViewById(R.id.edtxtHidInput);
			log_txt = (EditText) findViewById(R.id.log_txt);

			radioButton = (RadioButton) findViewById(R.id.rbSendData);

			mLog("Uruchomiono\nWybierz urz¹dzenie HID");
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
			filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
			registerReceiver(mUsbReceiver, filter);
			edtxtHidInput.setText("129");
			setupReceiver();
		} catch (Exception e) {
			Log.e("Init", "B³¹d uruchamiania", e);
		}
	}
	
	//konfiguracja receivera
	
	private void setupReceiver() {
		myTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				{
					try {
						if (connection != null && endPointRead != null) {
							final byte[] buffer = new byte[packetSize];
							final int status = connection.bulkTransfer(endPointRead, buffer, packetSize, 300);
							uiHandler.post(new Runnable() {
								@Override
								public void run() {
									if (status >= 0) {
										StringBuilder stringBuilder = new StringBuilder();
										if (receiveDataFormat.equals(INTEGER)) {
											for (int i = 0; i < packetSize; i++) {
												if (buffer[i] != 0) {
													stringBuilder.append(delimiter).append(String.valueOf(toInt(buffer[i])));
												} else {
													break;
												}
											}
										} else if (receiveDataFormat.equals(HEXADECIMAL)) {
											for (int i = 0; i < packetSize; i++) {
												if (buffer[i] != 0) {
													stringBuilder.append(delimiter).append(Integer.toHexString(buffer[i]));
												} else {
													break;
												}
											}
										} else if (receiveDataFormat.equals(TEXT)) {
											for (int i = 0; i < packetSize; i++) {
												if (buffer[i] != 0) {
													stringBuilder.append(String.valueOf((char) buffer[i]));
												} else {
													break;
												}
											}
										} else if (receiveDataFormat.equals(BINARY)) {
											for (int i = 0; i < packetSize; i++) {
												if (buffer[i] != 0) {
													stringBuilder.append(delimiter).append("0b").append(Integer.toBinaryString(Integer.valueOf(buffer[i])));
												} else {
													break;
												}
											}
										}
										stringBuilder.append("\notrzymano ").append(status).append(" bajtów");
										mLog(stringBuilder.toString());
									}
								}
							});
						}
					} catch (Exception e) {
						mLog("Wyj¹tek: " + e.getLocalizedMessage());
						Log.w("setupReceiver", e);
					}
				}
			};
		}, 0L, 1);
	}
	
	//wys³anie odpowiedniego znaku w zale¿noœci od wybranego przycisku
	
	public void onClick(View v) {
		
		if (v == button1) {
			if (device != null && endPointWrite != null && mUsbManager.hasPermission(device)) {
				
				String znak = "1";	
				
				byte[] out = znak.toString().getBytes();
																		
				mLog("Wysy³anie: " + znak.toString());
				if (radioButton.isChecked()) {
					try {
						String str[] = znak.toString().split("[\\s]");
						out = new byte[str.length];
						for (int i = 0; i < str.length; i++) {
							out[i] = toByte(Integer.decode(str[i]));
						}
					} catch (Exception e) {
						mLog("SprawdŸ bajty wys³ane jako tekst");
					}
				}
				int status = connection.bulkTransfer(endPointWrite, out, out.length, 250);
				mLog("wys³ano " + status + " bajtów");
				for (int i = 0; i < out.length; i++) {
					if (out[i] != 0) {
						mLogC(SPACE + toInt(out[i]));
					} else {
						break;
					}
				}
			}
		}
		if (v == button2) {
			if (device != null && endPointWrite != null && mUsbManager.hasPermission(device)) {
				
				String znak = "a";	
				
				byte[] out = znak.toString().getBytes();
																		
				mLog("Wysy³anie: " + znak.toString());
				if (radioButton.isChecked()) {
					try {
						String str[] = znak.toString().split("[\\s]");
						out = new byte[str.length];
						for (int i = 0; i < str.length; i++) {
							out[i] = toByte(Integer.decode(str[i]));
						}
					} catch (Exception e) {
						mLog("SprawdŸ bajty wys³ane jako tekst");
					}
				}
				int status = connection.bulkTransfer(endPointWrite, out, out.length, 250);
				mLog("wys³ano " + status + " bajtów");
				for (int i = 0; i < out.length; i++) {
					if (out[i] != 0) {
						mLogC(SPACE + toInt(out[i]));
					} else {
						break;
					}
				}
			}
		}
		if (v == button3) {
			if (device != null && endPointWrite != null && mUsbManager.hasPermission(device)) {
				
				String znak = "t";	
				
				byte[] out = znak.toString().getBytes();
																		
				mLog("Wysy³anie: " + znak.toString());
				if (radioButton.isChecked()) {
					try {
						String str[] = znak.toString().split("[\\s]");
						out = new byte[str.length];
						for (int i = 0; i < str.length; i++) {
							out[i] = toByte(Integer.decode(str[i]));
						}
					} catch (Exception e) {
						mLog("SprawdŸ bajty wys³ane jako tekst");
					}
				}
				int status = connection.bulkTransfer(endPointWrite, out, out.length, 250);
				mLog("wys³ano " + status + " bajtów");
				for (int i = 0; i < out.length; i++) {
					if (out[i] != 0) {
						mLogC(SPACE + toInt(out[i]));
					} else {
						break;
					}
				}
			}
		}
		if (v == button4) {
			if (device != null && endPointWrite != null && mUsbManager.hasPermission(device)) {
				
				String znak = "x";	
				
				byte[] out = znak.toString().getBytes();
																		
				mLog("Wysy³anie: " + znak.toString());
				if (radioButton.isChecked()) {
					try {
						String str[] = znak.toString().split("[\\s]");
						out = new byte[str.length];
						for (int i = 0; i < str.length; i++) {
							out[i] = toByte(Integer.decode(str[i]));
						}
					} catch (Exception e) {
						mLog("SprawdŸ bajty wys³ane jako tekst");
					}
				}
				int status = connection.bulkTransfer(endPointWrite, out, out.length, 250);
				mLog("wys³ano " + status + " bajtów");
				for (int i = 0; i < out.length; i++) {
					if (out[i] != 0) {
						mLogC(SPACE + toInt(out[i]));
					} else {
						break;
					}
				}
			}
		}
		if (v == button5) {
			if (device != null && endPointWrite != null && mUsbManager.hasPermission(device)) {
				
				String znak = "9";	
				
				byte[] out = znak.toString().getBytes();
																		
				mLog("Wysy³anie: " + znak.toString());
				if (radioButton.isChecked()) {
					try {
						String str[] = znak.toString().split("[\\s]");
						out = new byte[str.length];
						for (int i = 0; i < str.length; i++) {
							out[i] = toByte(Integer.decode(str[i]));
						}
					} catch (Exception e) {
						mLog("SprawdŸ bajty wys³ane jako tekst");
					}
				}
				int status = connection.bulkTransfer(endPointWrite, out, out.length, 250);
				mLog("wys³ano " + status + " bajtów");
				for (int i = 0; i < out.length; i++) {
					if (out[i] != 0) {
						mLogC(SPACE + toInt(out[i]));
					} else {
						break;
					}
				}
			}
		}
		if (v == button6) {
			if (device != null && endPointWrite != null && mUsbManager.hasPermission(device)) {
				
				String znak = "f";	
				
				byte[] out = znak.toString().getBytes();
																		
				mLog("Wysy³anie: " + znak.toString());
				if (radioButton.isChecked()) {
					try {
						String str[] = znak.toString().split("[\\s]");
						out = new byte[str.length];
						for (int i = 0; i < str.length; i++) {
							out[i] = toByte(Integer.decode(str[i]));
						}
					} catch (Exception e) {
						mLog("SprawdŸ bajty wys³ane jako tekst");
					}
				}
				int status = connection.bulkTransfer(endPointWrite, out, out.length, 250);
				mLog("wys³ano " + status + " bajtów");
				for (int i = 0; i < out.length; i++) {
					if (out[i] != 0) {
						mLogC(SPACE + toInt(out[i]));
					} else {
						break;
					}
				}
			}
		}
		if (v == button7) {
			if (device != null && endPointWrite != null && mUsbManager.hasPermission(device)) {
				
				String znak = "m";	
				
				byte[] out = znak.toString().getBytes();
																		
				mLog("Wysy³anie: " + znak.toString());
				if (radioButton.isChecked()) {
					try {
						String str[] = znak.toString().split("[\\s]");
						out = new byte[str.length];
						for (int i = 0; i < str.length; i++) {
							out[i] = toByte(Integer.decode(str[i]));
						}
					} catch (Exception e) {
						mLog("SprawdŸ bajty wys³ane jako tekst");
					}
				}
				int status = connection.bulkTransfer(endPointWrite, out, out.length, 250);
				mLog("wys³ano " + status + " bajtów");
				for (int i = 0; i < out.length; i++) {
					if (out[i] != 0) {
						mLogC(SPACE + toInt(out[i]));
					} else {
						break;
					}
				}
			}
		}
		if (v == btnClear) {
			log_txt.setText("");
		}
		if (v == btnSelectHIDDevice) {
			showListOfDevices();
		}
	}
	
	//wyœwietlenie listy urz¹dzeñ USB
	
	void showListOfDevices() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		if (mUsbManager.getDeviceList().size() == 0) {
			builder.setTitle(MESSAGE_CONNECT_YOUR_USB_HID_DEVICE);
		} else {
			builder.setTitle(MESSAGE_SELECT_YOUR_USB_HID_DEVICE);
		}
		List<CharSequence> list = new LinkedList<CharSequence>();
		for (UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
			list.add("devID:" + usbDevice.getDeviceId() + " VID:" + Integer.toHexString(usbDevice.getVendorId()) + " PID:" + Integer.toHexString(usbDevice.getProductId()) + " " + usbDevice.getDeviceName());
		}
		final CharSequence devicesName[] = new CharSequence[mUsbManager.getDeviceList().size()];
		list.toArray(devicesName);
		builder.setItems(devicesName, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				device = (UsbDevice) mUsbManager.getDeviceList().values().toArray()[which];
				mUsbManager.requestPermission(device, mPermissionIntent);
			}
		});
		builder.setCancelable(true);
		builder.show();
	}

	//odbiera ¿¹danie uprawnienia do pod³¹czania urz¹dzenia USB

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					setDevice(intent);
				}
			}
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				synchronized (this) {
					setDevice(intent);
				}
				if (device == null) {
					mLog("urz¹dzenie pod³¹czone");
				}
			}
			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				if (device != null) {
					device = null;
				}
				mLog("urz¹dzenie roz³¹czone");
			}
		}
		
		//ustanowienie po³¹czenia z konkretnym urz¹dzeniem USB
		
		private void setDevice(Intent intent) {
			device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			if (device != null && intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
				mLog("Wybrano urz¹dzenie");
				connection = mUsbManager.openDevice(device);
				intf = device.getInterface(0);
				if (null == connection) {
					mLog("(problem ze stabilnoœci¹ po³¹czenia)\n");
				} else {
					connection.claimInterface(intf, true);
				}
				try {
					if (UsbConstants.USB_DIR_OUT == intf.getEndpoint(1).getDirection()) {
						endPointWrite = intf.getEndpoint(1);
					}
				} catch (Exception e) {
					Log.e("endPointWrite", "Device have no endPointWrite", e);
				}
				try {
					if (UsbConstants.USB_DIR_IN == intf.getEndpoint(0).getDirection()) {
						endPointRead = intf.getEndpoint(0);
						packetSize = endPointRead.getMaxPacketSize();
					}
				} catch (Exception e) {
					Log.e("endPointWrite", "Device have no endPointRead", e);
				}
			}
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		receiveDataFormat = sharedPreferences.getString(RECEIVE_DATA_FORMAT, TEXT);
		setDelimiter();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		setSelectedMenuItemsFromSettings(menu);
		return true;
	}

	private void setSelectedMenuItemsFromSettings(Menu menu) {
		receiveDataFormat = sharedPreferences.getString(RECEIVE_DATA_FORMAT, TEXT);
		if (receiveDataFormat.equals(BINARY)) {
			menu.findItem(R.id.menuSettingsReceiveBinary).setChecked(true);
		} else if (receiveDataFormat.equals(INTEGER)) {
			menu.findItem(R.id.menuSettingsReceiveInteger).setChecked(true);
		} else if (receiveDataFormat.equals(HEXADECIMAL)) {
			menu.findItem(R.id.menuSettingsReceiveHexadecimal).setChecked(true);
		} else if (receiveDataFormat.equals(TEXT)) {
			menu.findItem(R.id.menuSettingsReceiveText).setChecked(true);
		}

		setDelimiter();
		if (settingsDelimiter.equals(DELIMITER_NONE)) {
			menu.findItem(R.id.menuSettingsDelimiterNone).setChecked(true);
		} else if (settingsDelimiter.equals(DELIMITER_NEW_LINE)) {
			menu.findItem(R.id.menuSettingsDelimiterNewLine).setChecked(true);
		} else if (settingsDelimiter.equals(DELIMITER_SPACE)) {
			menu.findItem(R.id.menuSettingsDelimiterSpace).setChecked(true);
		}
	}
	
	//dodatkowe menu (testy)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		item.setChecked(true);
		switch (item.getItemId()) {
		case R.id.menuSettingsReceiveBinary:
			editor.putString(RECEIVE_DATA_FORMAT, BINARY);
			editor.commit();
			break;
		case R.id.menuSettingsReceiveInteger:
			editor.putString(RECEIVE_DATA_FORMAT, INTEGER);
			editor.commit();
			break;
		case R.id.menuSettingsReceiveHexadecimal:
			editor.putString(RECEIVE_DATA_FORMAT, HEXADECIMAL);
			editor.commit();
			break;
		case R.id.menuSettingsReceiveText:
			editor.putString(RECEIVE_DATA_FORMAT, TEXT);
			editor.commit();
			break;
		case R.id.menuSettingsDelimiterNone:
			editor.putString(DELIMITER, DELIMITER_NONE);
			editor.commit();
			break;
		case R.id.menuSettingsDelimiterNewLine:
			editor.putString(DELIMITER, DELIMITER_NEW_LINE);
			editor.commit();
			break;
		case R.id.menuSettingsDelimiterSpace:
			editor.putString(DELIMITER, DELIMITER_SPACE);
			editor.commit();
			break;
		}

		receiveDataFormat = sharedPreferences.getString(RECEIVE_DATA_FORMAT, TEXT);
		setDelimiter();
		return true;
	}
	
	//zmiana na int
	
	private static int toInt(byte b) {
		return (int) b & 0xFF;
	}
	
	//zmiana na bajty
	
	private static byte toByte(int c) {
		return (byte) (c <= 0x7f ? c : ((c % 0x80) - 0x80));
	}

	private void setDelimiter() {
		settingsDelimiter = sharedPreferences.getString(DELIMITER, DELIMITER_NEW_LINE);
		if (settingsDelimiter.equals(DELIMITER_NONE)) {
			delimiter = "";
		} else if (settingsDelimiter.equals(DELIMITER_NEW_LINE)) {
			delimiter = NEW_LINE;
		} else if (settingsDelimiter.equals(DELIMITER_SPACE)) {
			delimiter = SPACE;
		}
	}

	private void mLog(String log) {
		log_txt.append(NEW_LINE);
		log_txt.append(log);
		log_txt.setSelection(log_txt.getText().length());
	}

	private void mLogC(String log) {
		log_txt.append(log);
		log_txt.setSelection(log_txt.getText().length());
	}

	private void setVersionToTitle() {
		try {
			this.setTitle(SPACE + this.getTitle() + SPACE + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}
