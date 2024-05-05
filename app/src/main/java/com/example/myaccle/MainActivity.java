package com.example.myaccle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView accsense;
    private TextView gyrosense;
    private TextView proximitySense;
    private TextView lightSense;
    private TextView magneticFieldSense;

    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private SensorManager sensorManager;
    private FileWriter fileWriter;
    private Button startButton;
    private Button stopButton;
    private Button exportButton; // Added export button

    private boolean isDataCollectionRunning = false;

    // Location variables
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextView references for sensor data
        accsense = findViewById(R.id.accsense);
        gyrosense = findViewById(R.id.gyrosense);
        proximitySense = findViewById(R.id.proximitySense);
        lightSense = findViewById(R.id.lightSense);
        latitudeTextView = findViewById(R.id.latitudeTextView);
        longitudeTextView = findViewById(R.id.longitudeTextView);
        magneticFieldSense = findViewById(R.id.magneticFieldSense);

        // Initialize SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Initialize start, stop, and export buttons
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        exportButton = findViewById(R.id.exportButton); // Initialize export button

        // Set click listener for export button
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExportButtonClick(v);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDataCollection();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDataCollection();
            }
        });

        // Disable stop button initially
        stopButton.setEnabled(false);

        // Create a file to store sensor data
        File file = new File(getExternalFilesDir(null), "sensor_data.txt");
        try {
            fileWriter = new FileWriter(file, true); // Append mode
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Request location permissions
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);

        // Initialize location manager and listener
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Update latitude and longitude TextViews
                latitudeTextView.setText("Latitude: " + location.getLatitude());
                longitudeTextView.setText("Longitude: " + location.getLongitude());

                // Log latitude and longitude data
                Log.d("SensorData", "Latitude: " + location.getLatitude());
                Log.d("SensorData", "Longitude: " + location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
    }

    private String getLogcatData() {
        StringBuilder log = new StringBuilder();
        try {
            // Execute the 'logcat' command
            Process process = Runtime.getRuntime().exec("logcat -d");

            // Read the output of the command
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            // Read each line of the output and append it to the StringBuilder
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return log.toString();
    }

    private void writeToCsv(String logData) {
        // Define the file name and path where the CSV file will be saved
        String fileName = "logcat_data.csv";
        String filePath = getExternalFilesDir(null) + File.separator + fileName;

        // Create a FileWriter object to write data to the CSV file
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write the logcat data to the CSV file
            writer.append(logData);
            writer.flush();

            // Show a toast message indicating successful file creation
            Toast.makeText(this, "Logcat data saved to " + filePath, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show a toast message if an error occurs during file writing
            Toast.makeText(this, "Error writing logcat data to CSV", Toast.LENGTH_SHORT).show();
        }
    }

    public void onExportButtonClick(View view) {
        // Get logcat data
        String logData = getLogcatData();

        // Write log data to CSV file
        writeToCsv(logData);

        // Provide feedback to user
        Toast.makeText(this, "Log data exported to CSV file", Toast.LENGTH_SHORT).show();
    }

    private void startDataCollection() {
        if (!isDataCollectionRunning) {
            // Register sensor listeners
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(this, "Accelerometer sensor not available", Toast.LENGTH_SHORT).show();
            }

            Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            if (gyroscope != null) {
                sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(this, "Gyroscope sensor not available", Toast.LENGTH_SHORT).show();
            }

            Sensor proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (proximity != null) {
                sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(this, "Proximity sensor not available", Toast.LENGTH_SHORT).show();
            }
            Sensor magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (magneticField != null) {
                sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(this, "Magnetic field sensor not available", Toast.LENGTH_SHORT).show();
            }

            Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if (light != null) {
                sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(this, "Light sensor not available", Toast.LENGTH_SHORT).show();
            }

            // Start location updates
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            isDataCollectionRunning = true;
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        }
    }

    private void stopDataCollection() {
        if (isDataCollectionRunning) {
            // Unregister sensor listeners
            sensorManager.unregisterListener(this);
            isDataCollectionRunning = false;
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            try {
                if (fileWriter != null) {
                    // Flush and close the FileWriter
                    fileWriter.flush();
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Stop location updates
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopDataCollection();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Log sensor data
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                Log.d("SensorData", "Accelerometer: X=" + event.values[0] + ", Y=" + event.values[1] + ", Z=" + event.values[2]);
                break;
            case Sensor.TYPE_GYROSCOPE:
                Log.d("SensorData", "Gyroscope: X=" + event.values[0] + ", Y=" + event.values[1] + ", Z=" + event.values[2]);
                break;
            case Sensor.TYPE_PROXIMITY:
                Log.d("SensorData", "Proximity: " + event.values[0]);
                break;
            case Sensor.TYPE_LIGHT:
                Log.d("SensorData", "Light: " + event.values[0]);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float magnitude = (float) Math.sqrt(x * x + y * y + z * z);
                Log.d("SensorData", "Magnetic Field: X=" + x + ", Y=" + y + ", Z=" + z + ", Magnitude=" + magnitude);
                break;
        }

        // Display sensor data
        String sensorName = "";
        String sensorData = "";

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                sensorName = "Accelerometer";
                sensorData = "Accelerometer:\nX: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2];
                break;
            case Sensor.TYPE_GYROSCOPE:
                sensorName = "Gyroscope";
                sensorData = "Gyroscope:\nX: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2];
                break;
            case Sensor.TYPE_PROXIMITY:
                sensorName = "Proximity";
                sensorData = "Proximity: " + event.values[0];
                break;
            case Sensor.TYPE_LIGHT:
                sensorName = "Light";
                sensorData = "Light: " + event.values[0];
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorName = "Magnetic Field";
                sensorData = "Magnetic Field:\nX: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2];
                break;
        }

        // Write sensor data to file
        writeSensorDataToFile(sensorName, sensorData);

        // Display sensor data
        displaySensorData(sensorName, sensorData);

        // Log latitude and longitude data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d("SensorData", "Latitude: " + latitudeTextView.getText());
            Log.d("SensorData", "Longitude: " + longitudeTextView.getText());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this implementation
    }

    private void displaySensorData(String sensorName, String sensorData) {
        // Display sensor data in respective TextViews
        switch (sensorName) {
            case "Accelerometer":
                accsense.setText(sensorData);
                break;
            case "Gyroscope":
                gyrosense.setText(sensorData);
                break;
            case "Proximity":
                proximitySense.setText(sensorData);
                break;
            case "Light":
                lightSense.setText(sensorData);
                break;
            case "Magnetic Field":
                magneticFieldSense.setText(sensorData);
                break;
        }
    }

    private void writeSensorDataToFile(String sensorName, String sensorData) {
        // Write sensor data to file
        try {
            if (fileWriter != null) {
                String data = "Sensor: " + sensorName + ", Data: " + sensorData + "\n";
                fileWriter.write(data);
                Log.d("SensorData", "Data written to file: " + data);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SensorData", "Error writing data to file: " + e.getMessage());
        }
    }
}
