package org.odk.collect.android.widgets;

import java.util.Date;

import org.digitalcampus.odk.collect.R;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.Chronology;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/**
 * 
 * @author Alex Little (alex@alexlittle.net)
 */
public class ANCApptDateWidget extends QuestionWidget {

	private final static String TAG = "ANCApptDateWidget";
	private DateTime lmp;
	
	private final static int ANC2_START = 168;
	private final static int ANC2_END = 203;
	private final static int ANC3_START = 210;
	private final static int ANC3_END = 231;
	private final static int ANC4_START = 238;
	private final static int ANC4_END = 259;
	
	private TextView nextVisitName;
	private TextView startEthiopicTV;
	private TextView endEthiopicTV;
	private TextView startGregorianTV;
	private TextView fromTV;
	private TextView toTV;
	
	private Chronology chron_eth = EthiopicChronology.getInstance();
	private Chronology chron_greg = GregorianChronology.getInstance();

	public ANCApptDateWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);

		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vv = vi.inflate(R.layout.anc_appt_calculator, null);
		addView(vv);

		// If there's an answer, use it.
		setAnswer();
	}

	private void setAnswer() {

		if (mPrompt.getAnswerValue() != null) {

			lmp = new DateTime(
					((Date) ((DateData) mPrompt.getAnswerValue()).getValue()).getTime());

			// set up the text strings
			nextVisitName = (TextView) findViewById(R.id.visitName);
			startEthiopicTV = (TextView) findViewById(R.id.startEthiopic);
			endEthiopicTV = (TextView) findViewById(R.id.endEthiopic);
			startGregorianTV = (TextView) findViewById(R.id.startGregorian);
			fromTV = (TextView) findViewById(R.id.appt_calc_from);
			toTV = (TextView) findViewById(R.id.appt_calc_to);
		
			setApptDates();

		}
	}

	
	private void setApptDates(){
		
		Resources res = getResources();
		// calculate no days difference between today and LMP
		DateTime today = new DateTime();
		DateMidnight todayM = today.toDateMidnight();
		Days d = Days.daysBetween(lmp, todayM);
		int difference = d.getDays();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMM yyyy");
		
		DateTime start = new DateTime();
		DateTime end = new DateTime();
		String visit = "";
		
		if (difference < 0){
			// future date for LMP entered - should go back and change
			nextVisitName.setText(res.getString(R.string.anc_appt_calc_lmpfuture));
			fromTV.setVisibility(GONE);
			toTV.setVisibility(GONE);
			return;
		} else if (difference == 0){
			// today date for LMP entered - should go back and change
			nextVisitName.setText(res.getString(R.string.anc_appt_calc_lmptoday));
			fromTV.setVisibility(GONE);
			toTV.setVisibility(GONE);
		} else if (difference < ANC2_START){
			//set for ANC2 dates
			start = lmp.plusDays(ANC2_START);
			end = lmp.plusDays(ANC2_END);
			visit = res.getString(R.string.anc_appt_calc_follow2);
		} else if (difference < ANC3_START){
			//set for ANC3 dates
			start = lmp.plusDays(ANC3_START);
			end = lmp.plusDays(ANC3_END);
			visit = res.getString(R.string.anc_appt_calc_follow3);
		} else if (difference < ANC4_START ){
			// set for ANC4 dates
			start = lmp.plusDays(ANC4_START);
			end = lmp.plusDays(ANC4_END);
			visit = res.getString(R.string.anc_appt_calc_follow4);
		} else {
			// too close to delivery date - no further appts
			nextVisitName.setText(res.getString(R.string.anc_appt_calc_lmptooclose));
			fromTV.setVisibility(GONE);
			toTV.setVisibility(GONE);
			return;
		}
		
		DateTime startGregorian = start.withChronology(chron_greg);
		DateTime startEthiopic = start.withChronology(chron_eth);
		DateTime endGregorian = end.withChronology(chron_greg);
		DateTime endEthiopic = end.withChronology(chron_eth);
		
		nextVisitName.setText(visit);
		
		String startEthio = String.format("%02d %s %04d", startEthiopic.getDayOfMonth(), 
				getResources().getStringArray(R.array.tigrinyan_months)[startEthiopic.getMonthOfYear()-1], 
				startEthiopic.getYear());
		String endEthio = String.format("%02d %s %04d", endEthiopic.getDayOfMonth(), 
						getResources().getStringArray(R.array.tigrinyan_months)[endEthiopic.getMonthOfYear()-1], 
						endEthiopic.getYear());
		
		
		String greg = String.format(res.getString(R.string.appt_calc_gregorian),fmt.print(startGregorian),fmt.print(endGregorian));
		
		startEthiopicTV.setText(startEthio);
		endEthiopicTV.setText(endEthio);
		startGregorianTV.setText(greg);

	}
	
	
	/**
	 * Resets date to blank
	 */
	@Override
	public void clearAnswer() {
	}

	@Override
	public IAnswerData getAnswer() {
		try {
			return new DateData(lmp.toDate());
		} catch (Exception e) {
		}
		return null;
	}

	@Override
	public void setFocus(Context context) {
		// Hide the soft keyboard if it's showing.
		InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {

	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
	}

}
