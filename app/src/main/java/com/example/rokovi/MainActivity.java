package com.example.rokovi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView DatumText;
    private TextView KonacniDatum;
    private Button Datum;
    private String[] rokovi;
    private AutoCompleteTextView dropdown;
    private TextInputLayout ErrorDropdown;
    private Button Izracunaj;
    private Date datum;
    private Vector<LocalDate> v = new Vector<>();

    @Override
    protected void onResume() {
        super.onResume();
        rokovi = getResources().getStringArray(R.array.Rokovi);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.dropdown_item, rokovi);
        dropdown.setAdapter(arrayAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DatumText = (TextView) findViewById(R.id.OdabraniDatum);
        Datum = (Button) findViewById(R.id.DatumGumb);
        dropdown = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        ErrorDropdown = (TextInputLayout) findViewById(R.id.textInputLayout);
        Izracunaj = (Button) findViewById(R.id.Izracunaj);
        KonacniDatum = (TextView) findViewById(R.id.KonacniDatum);

        Intent DohvatiDatum = getIntent();
        String DatumTxt = DohvatiDatum.getStringExtra("DatumText");
        DatumText.setText(DatumTxt);

        Datum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Izracunaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DatumText.length() == 0){
                    Datum.setError("Odaberite datum");
                }
                if(dropdown.getText().toString().equals("-")){
                    ErrorDropdown.setError("Odaberite broj dana roka");
                }
                if(DatumText.length() > 0 && !dropdown.getText().toString().equals("-")){
                    ErrorDropdown.setError(null);
                    //Datum iz stringa u Date
                    String test = DatumText.getText().toString();
                    String[] temp = test.split("-");
                    String dan = temp[0];
                    String mjesec = temp[1];
                    String godina = temp[2];
                    datum = new Date((Integer.parseInt(godina)-1900),(Integer.parseInt(mjesec) - 1),Integer.parseInt(dan));

                    //Rok iz dropdown liste
                    String rokString = dropdown.getText().toString();
                    LocalDate ldt = datum.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    if(rokString.equals("8 + 8")){
                        LocalDate noviDatum = ldt.plusDays(8);
                        int dan1 = noviDatum.getDayOfMonth();
                        int mjesec1 = noviDatum.getMonthValue();
                        String DatumZaProvjeru = String.valueOf(dan1) + "-" + String.valueOf(mjesec1);

                        //Provjera prvih 8 dana
                        Provjera(DatumZaProvjeru, noviDatum);
                        LocalDate DanRoka1 = v.get(0);
                        v.clear();

                        LocalDate DanRoka2 = DanRoka1.plusDays(8);
                        int dan2 = DanRoka2.getDayOfMonth();
                        int mjesec2 = DanRoka2.getMonthValue();
                        String DatumZaProvjeru2 = String.valueOf(dan2) + "-" + String.valueOf(mjesec2);

                        //Provjera drugih 8 dana
                        Provjera(DatumZaProvjeru2, DanRoka2);
                        LocalDate GotovRok = v.get(0);
                        v.clear();

                        LocalDate IduciDan = GotovRok.plusDays(1);
                        int danValjanosti = IduciDan.getDayOfMonth();
                        int mjesecValjanosti = IduciDan.getMonthValue();
                        String DatumValjanosti = String.valueOf(danValjanosti) + "-" + String.valueOf(mjesecValjanosti);
                        Provjera(DatumValjanosti, IduciDan);
                        LocalDate DatumZaIspis = v.get(0);
                        v.clear();

                        String Datum = DatumZaIspis.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        KonacniDatum.setText(Datum);
                    }
                    else{
                        int rokInt = Integer.parseInt(rokString);
                        LocalDate noviDatum = ldt.plusDays(rokInt);
                        int dan1 = noviDatum.getDayOfMonth();
                        int mjesec1 = noviDatum.getMonthValue();
                        String DatumZaProvjeru = String.valueOf(dan1) + "-" + String.valueOf(mjesec1);

                        //Provjera zadnjeg dana roka
                        Provjera(DatumZaProvjeru, noviDatum);
                        LocalDate DanRoka = v.get(0);
                        v.clear();

                        //Provjera dodatnog dana
                        LocalDate IduciDan = DanRoka.plusDays(1);
                        int danValjanosti = IduciDan.getDayOfMonth();
                        int mjesecValjanosti = IduciDan.getMonthValue();
                        String DatumValjanosti = String.valueOf(danValjanosti) + "-" + String.valueOf(mjesecValjanosti);
                        Provjera(DatumValjanosti, IduciDan);
                        LocalDate DatumZaIspis = v.get(0);
                        v.clear();

                        String Datum = DatumZaIspis.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        KonacniDatum.setText(Datum);
                    }
                }
            }
        });
    }

    private void Provjera(String DatumZaProvjeru, LocalDate noviDatum){
        String[] Praznici = {"1-1",
                "6-1",
                "4-4", //USKRS MIJENJA SE
                "5-4", //MIJENJA SE
                "1-5",
                "30-5",
                "3-6", //TJELOVO
                "22-6",
                "5-8",
                "15-8",
                "1-11",
                "18-11",
                "25-12",
                "26-12"
        };
        DayOfWeek dan = noviDatum.getDayOfWeek();
        for (String praznik : Praznici) {
            if(DatumZaProvjeru.equals(praznik)){
                LocalDate plusJedan = noviDatum.plusDays(1);
                int novidan = plusJedan.getDayOfMonth();
                int novimjesec = plusJedan.getMonthValue();
                String NoviDatumZaProvjeru = String.valueOf(novidan) + "-" + String.valueOf(novimjesec);
                Provjera(NoviDatumZaProvjeru, plusJedan);
                break;
            }
        }
        if((dan == DayOfWeek.SATURDAY) || (dan == DayOfWeek.SUNDAY)){
            LocalDate plusJedan = noviDatum.plusDays(1);
            int novidan = plusJedan.getDayOfMonth();
            int novimjesec = plusJedan.getMonthValue();
            String NoviDatumZaProvjeru = String.valueOf(novidan) + "-" + String.valueOf(novimjesec);
            Provjera(NoviDatumZaProvjeru, plusJedan);
        }
        v.add(noviDatum);
    }
}