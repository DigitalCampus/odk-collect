package org.odk.collect.android.widgets;

import java.util.Date;

import org.digitalcampus.odk.collect.R;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Period;
import org.joda.time.Weeks;
import org.joda.time.chrono.EthiopicChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Takes the input date (LMP) and displays the EDD in both Gregorian and
 * Ethiopic formats
 * 
 * Example usage:
 * 
 * <bind nodeset="/data/lmp" type="date" required="true()" />
 * <bind nodeset="/data/edd" calculate="date(/data/lmp+280)" type="date" required="true()" />
 * 
 * and
 * 
 * <input ref="/data/lmp" appearance="ethiopicDate">
 * 		<label ref="jr:itext('/data/lmp:label')" />
 * </input>
 * <input ref="/data/edd" appearance="ethiopicEDD">
 * 		<label ref="jr:itext('/data/edd:label')" />
 * </input>
 * 
 * @author Alex Little (alex@alexlittle.net)
 */
public class EthiopicEDDWidget extends QuestionWidget {

	private static final String TAG = "EthiopicEDDWidget";
	private DateTime lmp;
	private DateTime edd;
	private TextView lmpEthiopicTV;
	private TextView lmpGregorianTV;
	private TextView eddEthiopicTV;
	private TextView eddGregorianTV;
	private TextView gestationalAgeTV;

	private Chronology chron_eth = EthiopicChronology.getInstance();

	public EthiopicEDDWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);

		LayoutInflater vi = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vv = vi.inflate(R.layout.ethiopic_edd_calculator, null);
		addView(vv);

		// If there's an answer, use it.
		setAnswer();
	}

	private void setAnswer() {

		if (mPrompt.getAnswerValue() != null) {

			DateTimeFormatter fmt = DateTimeFormat.forPattern("d MMMM yyyy");

			// set up the text strings
			lmpEthiopicTV = (TextView) findViewById(R.id.lmpEthiopic);
			lmpGregorianTV = (TextView) findViewById(R.id.lmpGregorian);
			eddEthiopicTV = (TextView) findViewById(R.id.eddEthiopic);
			eddGregorianTV = (TextView) findViewById(R.id.eddGregorian);
			gestationalAgeTV = (TextView) findViewById(R.id.gestationalAge);
			
			// setup date object
			edd = new DateTime(
					((Date) ((DateData) mPrompt.getAnswerValue()).getValue())
							.getTime());
			
			// calculate and save LMP
			lmp = edd.minusDays(280);
			
			DateTime lmpGregorian = lmp.withChronology(GregorianChronology
					.getInstance());
			DateTime lmpEthiopic = lmp.withChronology(chron_eth);

			// display LMP (Ethio)
			lmpEthiopicTV.setText(String.format("%02d %s %04d", lmpEthiopic.getDayOfMonth(), 
					getResources().getStringArray(R.array.tigrinyan_months)[lmpEthiopic.getMonthOfYear()-1], 
					lmpEthiopic.getYear()));

			// display LMP (Greg)
			String strLMPGreg = fmt.print(lmpGregorian);
			lmpGregorianTV.setText("(" + strLMPGreg + ")");

			DateTime eddGregorian = edd.withChronology(GregorianChronology
					.getInstance());
			DateTime eddEthiopic = edd.withChronology(chron_eth);

			// display EDD (Ethio)
			eddEthiopicTV.setText(String.format("%02d %s %04d", eddEthiopic.getDayOfMonth(), 
					getResources().getStringArray(R.array.tigrinyan_months)[eddEthiopic.getMonthOfYear()-1],
					eddEthiopic.getYear()));

			// display EDD (Greg)
			String strEDDGreg = fmt.print(eddGregorian);
			eddGregorianTV.setText("(" + strEDDGreg + ")");
			
			// display Gestational Age
			DateTime today = new DateTime();
			Days days = Days.daysBetween(lmpGregorian, today);
			Weeks w = days.toStandardWeeks();

			// only display if it's 1 or more
			if ((w.getWeeks()) > 0){
				gestationalAgeTV.setText(getResources().getString(R.string.gestational_age_weeks,(w.getWeeks())));
			} else {
				gestationalAgeTV.setText(getResources().getString(R.string.gestational_age_none));
			}
		} else {
			Toast.makeText(getContext(), "Invalid input date", Toast.LENGTH_SHORT).show();
		}
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
			return new DateData(edd.toDate());
		} catch (Exception e) {
			Toast.makeText(getContext(), "Invalid date", Toast.LENGTH_SHORT)
					.show();
		}
		return null;
	}

	@Override
	public void setFocus(Context context) {
		// Hide the soft keyboard if it's showing.
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
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
