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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import android.view.View.OnClickListener;

public class MainActivity extends AppCompatActivity {
    //определение элементов управления
    Button startButton;
    Button stopButton;
    TextView textSens;
    TextView textStatus;
    TextView textWarning;

    //определение сенсоров
    SensorManager sensorManager;
    Sensor sensorAccel;
    Sensor sensorGyro;
    Sensor sensorMag;

    //определенние переменных для взятия времени
    private double mInitTime;
    private double sensTime;
    Timer timer, timer1;

    //определение переменных для записи показаний в файл
    private static String FILE_NAME = "sensorsValues";
    private static String FILE_PATH = "";
    StringBuilder sb = new StringBuilder();
    private static final int REQUEST_PERMISSION_WRITE = 1001;
    private boolean permissionGranted;

    //метка для начала/остановки записи в файл
    private boolean flagStatus = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //показ интерфейса и иницилизация Button
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);

        //установка слушателей кнопок
        OnClickListener listenerStart = new OnClickListener() {
            @Override
            public void onClick(View v) {
                flagStatus = true;
                textStatus.setText("Началась запись в файл " + FILE_NAME);

                // Часть, которая записывает данные после сигнала - перезаписывает то, что было до
                // Перезаписывает, так как writeValues пересоздаёт файл при её вызове
                // Нужно как-то переписать эту функцию, чтобы файл не создавался каждый раз, а дописывался
                // Пока не знаю как
//                timer1 = new Timer();
//                TimerTask task2 = new TimerTask() {
//                    @Override
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (flagStatus) {
//                                    sensTime = getDeltaT() / 1000;
//                                    writeValues();
//                                }
//                            }
//                        });
//                    }
//                };
//                timer1.schedule(task2, 0, 50);
//
//                timer1.cancel();

                // Спустя 5 секунд срабатывает сигнал - начинайте движение
                timer = new Timer();

                TimerTask task1 = new TimerTask(){
                    @Override
                    public void run() {
                        if (flagStatus) {
                            try {
                                Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notify);
                                r.play();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                timer.schedule(task1, 5000);

            }
        };

        OnClickListener listenerStop = new OnClickListener() {
            @Override
            public void onClick(View v) {
                flagStatus = false;
                textStatus.setText("Запись в файл " + FILE_NAME + " приостановлена");
            }
        };

        startButton.setOnClickListener(listenerStart);
        stopButton.setOnClickListener(listenerStop);

        //проверка разрешения на запись в файл
        if (!permissionGranted)
            checkPermissions();

        getExternalPath();

        textWarning = findViewById(R.id.textWarning);
        textStatus = findViewById(R.id.textStatus);
        textWarning.setText("Запустите сенсоры кнопкой \"Старт\"\nОстановите сенсоры кнопкой \"Стоп\"");

        //создание шапки таблицы
        createTableHead();

        //получение текущего времени
        mInitTime = System.currentTimeMillis();

        textSens = (TextView) findViewById(R.id.textSens);

        //получение сенсоров
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorMag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    //получение времени, прошедшего с начала инициализации приложения
    private double getDeltaT() {
        return System.currentTimeMillis() - mInitTime;
    }

    //формирование шапки таблицы с показаниями сенсоров
    public void createTableHead() {
        try {
            PrintWriter pw = new PrintWriter(new File(FILE_PATH));
            StringBuilder sb = new StringBuilder();
            sb.append("Time (s)");
            sb.append(",");
            sb.append("aAxisX (m/s2)");
            sb.append(",");
            sb.append("aAxisY (m/s2)");
            sb.append(",");
            sb.append("aAxisZ (m/s2)");
            sb.append(",");
            sb.append("gRotX (rad/s)");
            sb.append(",");
            sb.append("gRotY (rad/s)");
            sb.append(",");
            sb.append("gRotZ (rad/s)");
            sb.append(",");
            sb.append("magX (mT)");
            sb.append(",");
            sb.append("magY (mT)");
            sb.append(",");
            sb.append("magZ (mT)");

            pw.write(sb.toString());
            pw.close();
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorAccel,
                SensorManager.SENSOR_DELAY_FASTEST );
        sensorManager.registerListener(listener, sensorGyro, SensorManager.SENSOR_DELAY_FASTEST );
        sensorManager.registerListener(listener, sensorMag, SensorManager.SENSOR_DELAY_FASTEST );
        //запуск таймера и потока, повторяемого раз в 200 миллисекунд
        //если флаг записи в файл истинен, то содержимое потока выполнится
        //в противном случае нет

//        try {
//            Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notify);
//            r.play();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        timer = new Timer();
//
//        TimerTask task1 = new TimerTask(){
//            @Override
//            public void run() {
//                if (flagStatus) {
//                    try {
//                        Uri notify = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notify);
//                        r.play();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        timer.schedule(task1, 5000);
//
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

    //формирование строки для вывода на экран
    String format(double values[]) {
        return String.format(" \nTIME: %1$.3f\nX: %2$.8f\t\tY: %3$.8f\t\tZ: %4$.8f ", sensTime, values[0], values[1],
                values[2]);
    }

    //вывод информации на экран
    void showInfo() {
        sb.setLength(0);
        sb.append("Accelerometer " + format(valuesAccel))
                .append("\n\nGyroscope " + format(valuesGyro)).append("\n\nMagnetic Field " + format(valuesMag));
        textSens.setText(sb);
    }

    //вещественные массивы под значения сенсоров
    double[] valuesAccel = new double[3];
    double[] valuesGyro = new double[3];
    double[] valuesMag = new double[3];
    //установка слушателя сенсоров
    SensorEventListener listener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            //в зависимости от типа сенсора берутся его значения
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i = 0; i < 3; i++) {
                        valuesAccel[i] = event.values[i];
                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    for (int i = 0; i < 3; i++) {
                        valuesGyro[i] = event.values[i];
                    }
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    for (int i = 0; i < 3; i++) {
                        valuesMag[i] = event.values[i];
                    }
                    break;
            }

        }

    };

    //получение пути, куда буду записаны показания
    //по умолчанию файл будет записан во внутреннюю память (при полученном разрешении на запись)
    //маска итогового файла: sensorsValues XXXX.XX.XX XX-XX-XX.csv
    private void getExternalPath() {
        //получение пути до внутренней памяти
        File storage = Environment.getExternalStorageDirectory();
        String store = storage.getAbsolutePath();

        //получение текущей даты и времени
        DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH-mm-ss");
        Date date = new Date();

        FILE_NAME = FILE_NAME + " " + dateFormat.format(date) + ".csv";
        FILE_PATH = storage + "/" + FILE_NAME;
    }

    //запись показаний сенсоров
    //где запятые - парсеры для csv-файла
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

    //Проверка на доступность внешнего хранилища для чтения
    public boolean isExternalStorageWriteable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    //Проверка на доступность внешнего хранилища хотя бы только для чтения
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    //Проверка доступа к памяти
    private boolean checkPermissions() {

        if (!isExternalStorageReadable() || !isExternalStorageWriteable()) {
            Toast.makeText(this, "Внешнее хранилище не доступно", Toast.LENGTH_LONG).show();
            return false;
        }
        int permissionCheck = 1;//ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE);
            return false;
        }
        return true;
    }

    //Запрос на получение доступа к памяти
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
