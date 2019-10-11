package com.korzhueva.android.inertialnavigation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Определение элементов управления
    Button startButton;
    TextView tvTime;
    TextView tvAxis;
    TextView tvRotations;
    TextView tvMagnetic;

    // Оопределение датчиков
    SensorManager sensorManager;
    Sensor sensorAccel;
    Sensor sensorGyro;
    Sensor sensorMag;

    // Определенние переменных для взятия времени
    private double mInitTime;
    private double sensTime;
    Timer timer;

    // Определение переменных для записи показаний в файл
    private static String FILE_NAME = "sensorsValues";
    private static String FILE_PATH = "";
    StringBuilder sb = new StringBuilder();
    private static final int REQUEST_PERMISSION_WRITE = 1001;
    private boolean permissionGranted;

    // Метка для начала/остановки записи в файл
    private boolean flagStatus = false;
    private boolean isStart = false;

    private int sdk = android.os.Build.VERSION.SDK_INT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Показ интерфейса и иницилизация Button
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.btn_start);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvTime.setText(String.format("Время: %1$.3f\n", sensTime));
        tvAxis = (TextView) findViewById(R.id.tv_axis);
        tvRotations = (TextView) findViewById(R.id.tv_rotations);
        tvMagnetic = (TextView) findViewById(R.id.tv_magnetic);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStart) {
                    isStart = true;

                    startButton.setText("Стоп");

                    startButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_button_red_state));

                    // Получение текущего времени
                    mInitTime = System.currentTimeMillis();

                    createTableHead();

                    // Запись показаний разрешена
                    flagStatus = true;

                    // Задержка в 0,5 секунд для сбора показаний для калибровки
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            calibration();
                            // Получение текущего времени
                            mInitTime = System.currentTimeMillis();
                        }
                    }, 5000);

                    Snackbar.make(v, "Началась запись в файл " + FILE_NAME, Snackbar.LENGTH_LONG).show();
                } else {
                    isStart = false;

                    startButton.setText("Старт");

                    startButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_button_green_state));

                    // Запись показаний запрещена
                    flagStatus = false;

                    File file = new File(FILE_PATH);
                    FileWriter fr = null;
                    BufferedWriter br = null;
                    try {
                        fr = new FileWriter(file, true);
                        br = new BufferedWriter(fr);
                        br.newLine();
                        br.append("Stop of motion recording");
                        br.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            br.close();
                            fr.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Snackbar.make(v, "Запись в файл " + FILE_NAME + " приостановлена", Snackbar.LENGTH_LONG).show();
                }
            }
        });


        // Проверка разрешения на запись в файл
        if (!permissionGranted)
            checkPermissions();

        getExternalPath();

        // Получение показаний с датчиокв
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    // Получение времени, прошедшего с начала инициализации приложения
    private double getDeltaT() {
        return System.currentTimeMillis() - mInitTime;
    }

    // Калибровка показаний датчиков
    public void calibration() {
        try {
            // Запись в файл разделителя для разграничения калибровочных и последующих показний
            File file = new File(FILE_PATH);
            FileWriter fr = null;
            BufferedWriter br = null;
            try {
                fr = new FileWriter(file, true);
                br = new BufferedWriter(fr);
                br.newLine();
                br.append("Start of motion recording");
                br.newLine();
                br.append("Time (s)");
                br.append(",");
                br.append("aAxisX (m/s2)");
                br.append(",");
                br.append("aAxisY (m/s2)");
                br.append(",");
                br.append("aAxisZ (m/s2)");
                br.append(",");
                br.append("gRotX (rad/s)");
                br.append(",");
                br.append("gRotY (rad/s)");
                br.append(",");
                br.append("gRotZ (rad/s)");
                br.append(",");
                br.append("magX (mT)");
                br.append(",");
                br.append("magY (mT)");
                br.append(",");
                br.append("magZ (mT)");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Подаётся сигнал, позволяющий начать движение
            Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notify);
            r.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Формирование шапки таблицы с показаниями датчиков
    public void createTableHead() {
        File file = new File(FILE_PATH);
        FileWriter fr = null;
        BufferedWriter br = null;
        try {
            fr = new FileWriter(file, true);
            br = new BufferedWriter(fr);
            br.append("Calibration");
            br.newLine();
            br.append("Time (s)");
            br.append(",");
            br.append("aAxisX (m/s2)");
            br.append(",");
            br.append("aAxisY (m/s2)");
            br.append(",");
            br.append("aAxisZ (m/s2)");
            br.append(",");
            br.append("gRotX (rad/s)");
            br.append(",");
            br.append("gRotY (rad/s)");
            br.append(",");
            br.append("gRotZ (rad/s)");
            br.append(",");
            br.append("magX (mT)");
            br.append(",");
            br.append("magY (mT)");
            br.append(",");
            br.append("magZ (mT)");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorAccel,
                SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(listener, sensorGyro, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(listener, sensorMag, SensorManager.SENSOR_DELAY_FASTEST);
        // Запуск таймера и потока, повторяемого раз в 200 миллисекунд
        // Если флаг записи в файл истинен, то содержимое потока выполнится
        // В противном случае - не выполняется

        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (flagStatus) {
                            sensTime = getDeltaT() / 1000;
                            writeValues();
                            showInfo();
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, 50);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
        timer.cancel();
    }

    // Запись данных с датчиков
    // Где запятые - парсеры для csv-файла
    public void writeValues() {
        File file = new File(FILE_PATH);
        FileWriter fr = null;
        BufferedWriter br = null;
        try {
            fr = new FileWriter(file, true);
            br = new BufferedWriter(fr);
            br.newLine();
            br.append(String.valueOf(sensTime));
            br.append(',');
            br.append(String.valueOf(valuesAccel[0]));
            br.append(',');
            br.append(String.valueOf(valuesAccel[1]));
            br.append(',');
            br.append(String.valueOf(valuesAccel[2]));
            br.append(',');
            br.append(String.valueOf(valuesGyro[0]));
            br.append(',');
            br.append(String.valueOf(valuesGyro[1]));
            br.append(',');
            br.append(String.valueOf(valuesGyro[2]));
            br.append(',');
            br.append(String.valueOf(valuesMag[0]));
            br.append(',');
            br.append(String.valueOf(valuesMag[1]));
            br.append(',');
            br.append(String.valueOf(valuesMag[2]));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    // Формирование строки для вывода на экран
    String format(double values[]) {
        return String.format("X: %2$.8f\tY: %3$.8f\tZ: %4$.8f", sensTime, values[0], values[1],
                values[2]);
    }

    // Вывод информации на экран
    void showInfo() {
        tvTime.setText(String.format("Время: %1$.3f\n", sensTime));

        tvAxis.setText("Акселерометр\n" + format(valuesAccel));

        tvRotations.setText("Гироскоп\n" + format(valuesGyro));

        tvMagnetic.setText("Магнитометр\n" + format(valuesMag));
    }

    // Вещественные массивы под показания датчиков
    double[] valuesAccel = new double[3];
    double[] valuesGyro = new double[3];
    double[] valuesMag = new double[3];

    // Установка слушателя датчиков
    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override

        public void onSensorChanged(SensorEvent event) {
            // В зависимости от типа датчика снимаются его показания
            switch (event.sensor.getType()) {
                // Акселерометр
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i = 0; i < 3; i++) {
                        valuesAccel[i] = event.values[i];
                    }
                    break;

                // Гироскоп
                case Sensor.TYPE_GYROSCOPE:
                    for (int i = 0; i < 3; i++) {
                        valuesGyro[i] = event.values[i];
                    }
                    break;

                // Магнитометр
                case Sensor.TYPE_MAGNETIC_FIELD:
                    for (int i = 0; i < 3; i++) {
                        valuesMag[i] = event.values[i];
                    }
                    break;
            }

        }

    };

    // Получение пути, куда буду записаны показания
    // По умолчанию файл будет записан во внутреннюю память (при полученном разрешении на запись)
    // Маска итогового файла: sensorsValues XXXX.XX.XX XX-XX-XX.csv
    private void getExternalPath() {
        //получение пути до внутренней памяти
        File storage = Environment.getExternalStorageDirectory();

        // Получение текущей даты и времени
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH-mm-ss");
        Date date = new Date();

        FILE_NAME = FILE_NAME + " " + dateFormat.format(date) + ".csv";
        FILE_PATH = storage + "/" + FILE_NAME;
    }

    // Проверка на доступность внешнего хранилища для чтения
    public boolean isExternalStorageWriteable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Проверка на доступность внешнего хранилища хотя бы только для чтения
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    // Проверка доступа к памяти
    private boolean checkPermissions() {

        if (!isExternalStorageReadable() || !isExternalStorageWriteable()) {
            Toast.makeText(this, "Внешнее хранилище недоступно", Toast.LENGTH_LONG).show();
            return false;
        }
        int permissionCheck = 1; //ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE);
            return false;
        }

        return true;
    }

    // Запрос на получение доступа к памяти
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    permissionGranted = true;
                break;
        }
    }
}
