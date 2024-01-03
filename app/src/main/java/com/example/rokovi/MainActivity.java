package com.example.rokovi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView DatumText;
    private TextView KonacniDatum;
    private TextView Info;
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
        dropdown.setDropDownHeight((int) (Resources.getSystem().getDisplayMetrics().heightPixels / 2.4));
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
        Info = (TextView) findViewById(R.id.info);

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

        dropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
                String rok = parent.getItemAtPosition(position).toString();
                switch (rok) {
                    case "8":
                        Info.setText("8. dan roka ne smije biti vikend/praznik. Aplikacija vraca prvi radni dan nakon 8. dana roka.");
                        break;
                    case "15":
                        Info.setText("15. dan roka ne smije biti vikend/praznik. Aplikacija vraca prvi radni dan nakon 15. dana roka.");
                        break;
                    case "8 + 8":
                        Info.setText("Dva puta se vrti 8 dana roka, prvi i drugi 8. dan roka ne smije biti vikend/praznik. Aplikacija vraca prvi radni dan nakon drugog 8. dana roka.");
                        break;
                    case "30":
                        Info.setText("30. dan roka ne smije biti vikend/praznik. Aplikacija vraca prvi radni dan nakon 30. dana roka.");
                        break;
                    case "90":
                        Info.setText("90. dan roka smije biti vikend/praznik. Aplikacija vraca zadnji dan, odnosno 90. dan roka.");
                        break;
                    case "-8":
                        Info.setText("Racunanje roka unazad, 8. dan roka ne smije biti vikend/praznik. Aplikacija vraca prvi prijasnji radni dan prije 8. dana roka.");
                        break;
                    case "-30":
                        Info.setText("Racunanje roka unazad, 30. dan roka ne smije biti vikend/praznik. Aplikacija vraca prvi prijasnji radni dan prije 30. dana roka.");
                        break;
                    case "-":
                        Info.setText("");
                        break;
                }

            }
        });

        Izracunaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (DatumText.length() == 0) {
                    Datum.setError("Odaberite datum");
                }
                if (dropdown.getText().toString().equals("-")) {
                    ErrorDropdown.setError("Odaberite broj dana roka");
                }
                if (DatumText.length() > 0 && !dropdown.getText().toString().equals("-")) {
                    ErrorDropdown.setError(null);
                    //Datum iz stringa u Date
                    String test = DatumText.getText().toString();
                    String[] temp = test.split("-");
                    String dan = temp[0];
                    String mjesec = temp[1];
                    String godina = temp[2];
                    datum = new Date((Integer.parseInt(godina) - 1900), (Integer.parseInt(mjesec) - 1), Integer.parseInt(dan));

                    //Rok iz dropdown liste
                    String rokString = dropdown.getText().toString();
                    LocalDate ldt = datum.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    if (rokString.equals("8 + 8")) {
                        LocalDate noviDatum = ldt.plusDays(8);
                        int dan1 = noviDatum.getDayOfMonth();
                        int mjesec1 = noviDatum.getMonthValue();
                        String DatumZaProvjeru = String.valueOf(dan1) + "-" + String.valueOf(mjesec1);

                        //Provjera prvih 8 dana
                        Provjera(DatumZaProvjeru, noviDatum, false);
                        LocalDate DanRoka1 = v.get(0);
                        v.clear();

                        LocalDate DanRoka2 = DanRoka1.plusDays(8);
                        int dan2 = DanRoka2.getDayOfMonth();
                        int mjesec2 = DanRoka2.getMonthValue();
                        String DatumZaProvjeru2 = String.valueOf(dan2) + "-" + String.valueOf(mjesec2);

                        //Provjera drugih 8 dana
                        Provjera(DatumZaProvjeru2, DanRoka2, false);
                        LocalDate GotovRok = v.get(0);
                        v.clear();

                        LocalDate IduciDan = GotovRok.plusDays(1);
                        int danValjanosti = IduciDan.getDayOfMonth();
                        int mjesecValjanosti = IduciDan.getMonthValue();
                        String DatumValjanosti = String.valueOf(danValjanosti) + "-" + String.valueOf(mjesecValjanosti);
                        Provjera(DatumValjanosti, IduciDan, false);
                        LocalDate DatumZaIspis = v.get(0);
                        v.clear();

                        String Datum = DatumZaIspis.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        KonacniDatum.setText(Datum);
                    } else if (rokString.contains("-")) {
                        String bezMinusa = rokString.substring(1);
                        int rokInt = Integer.parseInt(bezMinusa);
                        LocalDate noviDatum = ldt.minusDays(rokInt);

                        //minus jos jedan dan i provjera
                        LocalDate IduciDan = noviDatum.minusDays(1);
                        int danValjanosti = IduciDan.getDayOfMonth();
                        int mjesecValjanosti = IduciDan.getMonthValue();
                        String DatumValjanosti = String.valueOf(danValjanosti) + "-" + String.valueOf(mjesecValjanosti);
                        Provjera(DatumValjanosti, IduciDan, true);
                        LocalDate DatumZaIspis = v.get(0);
                        v.clear();

                        String Datum = DatumZaIspis.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        KonacniDatum.setText(Datum);
                    } else if (rokString.equals("90")) {
                        LocalDate noviDatum = ldt.plusDays(89);
                        String Datum = noviDatum.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        KonacniDatum.setText(Datum);
                    } else {
                        int rokInt = Integer.parseInt(rokString);
                        LocalDate noviDatum = ldt.plusDays(rokInt);
                        int dan1 = noviDatum.getDayOfMonth();
                        int mjesec1 = noviDatum.getMonthValue();
                        String DatumZaProvjeru = String.valueOf(dan1) + "-" + String.valueOf(mjesec1);

                        //Provjera zadnjeg dana roka
                        Provjera(DatumZaProvjeru, noviDatum, false);
                        LocalDate DanRoka = v.get(0);
                        v.clear();

                        //Provjera dodatnog dana
                        LocalDate IduciDan = DanRoka.plusDays(1);
                        int danValjanosti = IduciDan.getDayOfMonth();
                        int mjesecValjanosti = IduciDan.getMonthValue();
                        String DatumValjanosti = String.valueOf(danValjanosti) + "-" + String.valueOf(mjesecValjanosti);
                        Provjera(DatumValjanosti, IduciDan, false);
                        LocalDate DatumZaIspis = v.get(0);
                        v.clear();

                        String Datum = DatumZaIspis.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        KonacniDatum.setText(Datum);
                    }
                }
            }
        });
    }

    public String read_file(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
    }

    private void Provjera(String DatumZaProvjeru, LocalDate noviDatum, boolean minus) {
        String procitaniPraznici = read_file(this, "praznici.txt");
        String[] Praznici = procitaniPraznici.split(",");

        DayOfWeek dan = noviDatum.getDayOfWeek();
        for (String praznik : Praznici) {
            if (DatumZaProvjeru.equals(praznik)) {
                LocalDate plusJedan;
                if (minus) {
                    plusJedan = noviDatum.minusDays(1);
                } else {
                    plusJedan = noviDatum.plusDays(1);
                }
                int novidan = plusJedan.getDayOfMonth();
                int novimjesec = plusJedan.getMonthValue();
                String NoviDatumZaProvjeru = String.valueOf(novidan) + "-" + String.valueOf(novimjesec);
                Provjera(NoviDatumZaProvjeru, plusJedan, minus);
                break;
            }
        }
        if ((dan == DayOfWeek.SATURDAY) || (dan == DayOfWeek.SUNDAY)) {
            LocalDate plusJedan;
            if (minus) {
                plusJedan = noviDatum.minusDays(1);
            } else {
                plusJedan = noviDatum.plusDays(1);
            }
            int novidan = plusJedan.getDayOfMonth();
            int novimjesec = plusJedan.getMonthValue();
            String NoviDatumZaProvjeru = String.valueOf(novidan) + "-" + String.valueOf(novimjesec);
            Provjera(NoviDatumZaProvjeru, plusJedan, minus);
        }
        v.add(noviDatum);
    }
}