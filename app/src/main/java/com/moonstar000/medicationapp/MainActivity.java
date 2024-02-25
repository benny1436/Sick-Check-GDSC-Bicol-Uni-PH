package com.moonstar000.medicationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.privacysandbox.tools.core.model.Method;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;





public class MainActivity extends AppCompatActivity {

    LinearLayout medicationList;
    EditText sicknessInput;
    TextView possibleDiagnosis, drugName, genericName, dose, form, route, frequency, indication, sideEffect, warnings, notes;
    Button submitBtn;

    ProgressBar medication_progress_bar;

    private MyTensorFlowModel tfLiteModel;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submitBtn = findViewById(R.id.submit_btn);
        sicknessInput = findViewById(R.id.sickness_input);
        medicationList = findViewById(R.id.medication_linear_layout);

        possibleDiagnosis = findViewById(R.id.possible_diagnosis);
        drugName = findViewById(R.id.drug_name);
        genericName = findViewById(R.id.generic_name);
        dose = findViewById(R.id.dose);
        form = findViewById(R.id.form);
        route = findViewById(R.id.route);
        frequency = findViewById(R.id.frequency);
        indication = findViewById(R.id.indication);
        sideEffect = findViewById(R.id.side_effect);
        warnings = findViewById(R.id.warnings);
        notes = findViewById(R.id.notes);

        medication_progress_bar = findViewById(R.id.medication_progress_bar);

        // http request sample code



        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.9f;
        configBuilder.topK = 1;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 2048;


        GenerationConfig generationConfig = configBuilder.build();


        List<SafetySetting> safetySettings = new ArrayList<>();

        safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.LOW_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.LOW_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.LOW_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.LOW_AND_ABOVE));

        // For text-only input, use the gemini-pro model
        GenerativeModel gm = new GenerativeModel("gemini-1.0-pro", "AIzaSyCKP_XK7lJmfCy44UTjrpjzikZoF-ixmdo", generationConfig, safetySettings);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);


        submitBtn.setOnClickListener(v -> {

            final String[] illness = {""};

            String urlString = "http://46.250.253.229:5000/request?extra=" + sicknessInput.getText().toString();;

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://www.google.com";

// Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response2) {
                            // Display the first 500 characters of the response string.
                            System.out.println("Response is: " + response2);
                            illness[0] = response2;
                            Toast.makeText(MainActivity.this, "Response is: " + response2, Toast.LENGTH_LONG).show();
                            medication_progress_bar.setVisibility(View.VISIBLE);

                            String input = illness[0];
                            Content content = new Content.Builder()
                                    .addText("Find possible medication for an illness provided in the input, output using a JSON Template:{  \"name\": \"Medication Name\",  \"genericName\": \"Generic Name\",  \"drugClass\": \"Drug Class\",  \"dose\": {    \"amount\": 10,    \"unit\": \"mg\"  },  \"form\": \"Tablet\",  \"route\": \"Oral\",  \"frequency\": {    \"timesPerDay\": 2,    \"period\": \"Day\"  },  \"indication\": {    \"condition\": \"Condition Treated\",    \"severity\": \"Moderate\"  },  \"sideEffect\": {    \"name\": \"Nausea\",    \"severity\": \"Moderate\"  },  \"warnings\": [    \"Do not take with alcohol\"  ],  \"notes\": \"Take after food\"}")
                                    .addText("input: " + input)
                                    .addText("output: ")
                                    .build();
                            Executor executor = new Executor() {
                                @Override
                                public void execute(@NonNull Runnable command) {
                                    command.run();
                                }
                            };
                            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
                            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                                @Override
                                public void onSuccess(GenerateContentResponse result) {
                                    String resultText = result.getText();
                                    System.out.println(resultText);

                                    try {
                                        Medication medication = MedicationParser.parseMedication(resultText);

                                        // Access data using getters:
                                        System.out.println("Name: " + medication.name);
                                        System.out.println("Generic name: " + medication.genericName);
                                        System.out.println("Dose: " + medication.dose.amount + " " + medication.dose.unit);

                                        possibleDiagnosis.setText("Possible Diagnosis: "+ illness[0]);
                                        drugName.setText("Drug Name: " + medication.name);
                                        genericName.setText("Generic name: " + medication.genericName);
                                        dose.setText("Dose: " + medication.dose.amount + " " + medication.dose.unit);
                                        form.setText("Form:" + medication.form);
                                        route.setText("Route: " + medication.route);
                                        frequency.setText("Frequency: " + medication.frequency.timesPerDay + " times per " + medication.frequency.period);
                                        indication.setText("Indication: " + medication.indication.condition + " " + medication.indication.severity);
                                        sideEffect.setText("Side Effect: " + medication.sideEffect.name + " " + medication.sideEffect.severity);
                                        if (medication.warnings.size() > 0) {
                                            warnings.setText("Warnings: " + medication.warnings.get(0));
                                        } else {
                                            warnings.setText("Warnings: None");
                                        }
                                        notes.setText("Notes: " + medication.notes);

                                        medication_progress_bar.setVisibility(View.GONE);


                                    } catch (JsonParseException e) {
                                        Toast.makeText(MainActivity.this, "Invalid JSON format", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    t.printStackTrace();
                                }
                            }, executor);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("That didn't work!");
                    Toast.makeText(MainActivity.this, "That didn't work!", Toast.LENGTH_SHORT).show();
                }
            });

// Add the request to the RequestQueue.
            queue.add(stringRequest);







        });


    }




    public static class MedicationParser {

        public static Medication parseMedication(String jsonString) throws JsonParseException {
            Gson gson = new Gson();
            try {
                return gson.fromJson(jsonString, Medication.class);
            } catch (JsonSyntaxException e) {
                throw new JsonParseException("Invalid JSON format", e);
            }
        }
    }


    public class Medication {
        private String name;
        private String genericName;
        private String drugClass;
        private Dose dose;
        private String form;
        private String route;
        private Frequency frequency;
        private Indication indication;
        private SideEffect sideEffect;
        private List<String> warnings;
        private String notes;

        // Getters and setters (omitted for brevity)
    }

    public class Dose {
        private double amount;
        private String unit;

        // Getters and setters (omitted for brevity)
    }

    public class Frequency {
        private int timesPerDay;
        private String period;

        // Getters and setters (omitted for brevity)
    }

    public class Indication {
        private String condition;
        private String severity;

        // Getters and setters (omitted for brevity)
    }

    public class SideEffect {
        private String name;
        private String severity;

        // Getters and setters (omitted for brevity)
    }


}