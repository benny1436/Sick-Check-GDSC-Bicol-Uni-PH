package com.moonstar000.medicationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;


import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;


public class MainActivity extends AppCompatActivity {

    LinearLayout medicationList;
    EditText sicknessInput;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        submitBtn = findViewById(R.id.submit_btn);
        sicknessInput = findViewById(R.id.sickness_input);

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

            String input = sicknessInput.getText().toString();
            Content content = new Content.Builder()
                    .addText("Find possible medication for an illness provided in the input, output using a JSON Template:{  \"name\": \"Medication Name\",  \"genericName\": \"Generic Name\",  \"drugClass\": \"Drug Class\",  \"dose\": {    \"amount\": 10,    \"unit\": \"mg\"  },  \"form\": \"Tablet\",  \"route\": \"Oral\",  \"frequency\": {    \"timesPerDay\": 2,    \"period\": \"Day\"  },  \"indication\": {    \"condition\": \"Condition Treated\",    \"severity\": \"Moderate\"  },  \"contraindications\": [\"Allergy to X\"],  \"interactions\": [\"Drug Y\"],  \"sideEffects\": [    {      \"name\": \"Headache\",      \"severity\": \"Mild\"    },    {      \"name\": \"Nausea\",      \"severity\": \"Moderate\"    }  ],  \"warnings\": [\"Do not take with alcohol\"],  \"notes\": \"Take after food\"}")
                    .addText("input: Soar Throat")
                    .addText("output: {\n  \"name\": \"Ibuprofen\",\n  \"genericName\": \"Ibuprofen\",\n  \"drugClass\": \"Nonsteroidal anti-inflammatory drug (NSAID)\",\n  \"dose\": {\n    \"amount\": 200,\n    \"unit\": \"mg\"\n  },\n  \"form\": \"Tablet\",\n  \"route\": \"Oral\",\n  \"frequency\": {\n    \"timesPerDay\": 3,\n    \"period\": \"Day\"\n  },\n  \"indication\": {\n    \"condition\": \"Soar Throat\",\n    \"severity\": \"Moderate\"\n  },\n  \"contraindications\": [],\n  \"interactions\": [],\n  \"sideEffects\": [\n    {\n      \"name\": \"Headache\",\n      \"severity\": \"Mild\"\n    },\n    {\n      \"name\": \"Nausea\",\n      \"severity\": \"Mild\"\n    }\n  ],\n  \"warnings\": [],\n  \"notes\": \"Take after food\"\n}")
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

                        medicationList = findViewById(R.id.medication_linear_layout);

                        medicationList.removeAllViews();

//                    medicationList.addView(new AppCompatTextView(MainActivity.this) {{
//                        setText(resultText);
//                        setTypeface(null, Typeface.BOLD);
//                        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
//                        setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
//                    }});

                        // Add a new textview to the linear layout
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Drug Name: " + medication.name);
                            setTypeface(null, Typeface.BOLD);
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Generic name: " + medication.genericName);
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Dose: " + medication.dose.amount + " " + medication.dose.unit);
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Form:" + medication.form);
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Route:" + medication.route);
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});

                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Indication:" + medication.indication.condition + " " + medication.indication.severity);
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Contraindications:" + Arrays.toString(medication.contraindications.toArray()));
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Interactions:" + Arrays.toString(medication.interactions.toArray()));
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Side Effects:" + medication.sideEffects.get(0).name + " " + medication.sideEffects.get(0).severity);
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Warnings:" + medication.warnings.get(0));
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Notes:" + medication.notes);
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});
                        medicationList.addView(new AppCompatTextView(MainActivity.this) {{
                            setText("Frequency:" + medication.frequency.timesPerDay + " times per " + medication.frequency.period);
                            setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
                        }});

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
        private List<String> contraindications;
        private List<String> interactions;
        private List<SideEffect> sideEffects;
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