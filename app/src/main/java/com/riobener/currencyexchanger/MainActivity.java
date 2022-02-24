package com.riobener.currencyexchanger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;



import com.riobener.currencyexchanger.databinding.ActivityMainBinding;
import com.riobener.currencyexchanger.domain.model.CurrencyDTO;
import com.riobener.currencyexchanger.domain.viewmodel.MainViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainViewModel = new MainViewModel(getApplication());
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        if (checkConnection(getApplication().getApplicationContext())) {
            setContentView(view);
        } else {
            binding.BtnSave.setVisibility(View.GONE);
            binding.BtnUpdate.setVisibility(View.GONE);
            binding.SpinnerConvertTo.setAdapter(null);
            binding.SpinnerConvertRes.setAdapter(null);
            mainViewModel.getConvertingCurrenciesFromDb().observe(this, v -> {
                List<String> convertingCurrency = new ArrayList<>();
                for (CurrencyDTO currencyDTO: v) {
                    convertingCurrency.add(currencyDTO.getConvertingCurrency());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        convertingCurrency);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.SpinnerConvertTo.setAdapter(adapter);
            });

            binding.SpinnerConvertTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    mainViewModel.getCurrencyToConvertFromDb(adapterView.getItemAtPosition(i).
                            toString()).observe(MainActivity.this, v-> {
                        List<String> currencyToConvert = new ArrayList<>();
                        for (CurrencyDTO currencyDTO: v) {
                            currencyToConvert.add(currencyDTO.getCurrencyToConvert());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                MainActivity.this,
                                android.R.layout.simple_spinner_item,
                                currencyToConvert);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.SpinnerConvertRes.setAdapter(adapter);
                    });
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
        setContentView(view);
    }
    private static final DecimalFormat df = new DecimalFormat("0.00");
    public void onClickConvert(View view){
        if(isNumeric(binding.EtConvertTo.getText().toString())){
            String currencyTo;
            String currencyRes;
            if (checkConnection(getApplication().getApplicationContext())) {
                currencyTo = binding.SpinnerConvertTo.getSelectedItem().toString();
                currencyRes = binding.SpinnerConvertRes.getSelectedItem().toString();
                String finalCurrencyTo = currencyTo;
                String finalCurrencyRes = currencyRes;
                mainViewModel.getValue(currencyTo, currencyRes).observe(this,
                        conversionRate -> {
                            double result;
                            result = Double.parseDouble(binding.EtConvertTo.getText().toString())
                                    * conversionRate;
                            binding.EtConvertRes.setText(df.format(result));
                        });
            } else {
                currencyTo = binding.SpinnerConvertTo.getSelectedItem().toString();
                currencyRes = binding.SpinnerConvertRes.getSelectedItem().toString();
                mainViewModel.getValueFromDb(currencyTo, currencyRes).observe(this,
                        conversionRate -> {
                            double result;
                            result = Double.parseDouble(binding.EtConvertTo.getText().toString())
                                    * conversionRate;
                            binding.EtConvertRes.setText(Double.toString(result));
                        });
            }
        }else{
            Toast.makeText(this,"You can only write numbers!",Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSave(View view) {
        String currencyTo;
        String currencyRes;
        currencyTo = getResources().getStringArray(R.array.currency)
                [binding.SpinnerConvertTo.getSelectedItemPosition()];
        currencyRes = getResources().getStringArray(R.array.currency)
                [binding.SpinnerConvertRes.getSelectedItemPosition()];
        String finalCurrencyTo = currencyTo;
        String finalCurrencyRes = currencyRes;
        mainViewModel.getValue(currencyTo, currencyRes).observe(this, conversionRate -> {
            mainViewModel.getValueFromDb(finalCurrencyTo, finalCurrencyRes).observe(this,
                    conversionRateFromDb ->{
                        if (conversionRateFromDb == null) {
                            CurrencyDTO currencyDTO = new CurrencyDTO(finalCurrencyTo,
                                    finalCurrencyRes, conversionRate);
                            mainViewModel.saveCurrency(currencyDTO);
                        }
                    });
        });
    }

    public void onClickUpdate(View view) {
        String currencyTo;
        String currencyRes;
        currencyTo = getResources().getStringArray(R.array.currency)
                [binding.SpinnerConvertTo.getSelectedItemPosition()];
        currencyRes = getResources().getStringArray(R.array.currency)
                [binding.SpinnerConvertRes.getSelectedItemPosition()];
        String finalCurrencyTo = currencyTo;
        String finalCurrencyRes = currencyRes;
        mainViewModel.getValue(currencyTo, currencyRes).observe(this, conversionRate -> {
            new Thread(() -> mainViewModel.updateCurrency(conversionRate,
                    finalCurrencyTo,
                    finalCurrencyRes)).start();
        });
    }

    public static boolean checkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

            if (activeNetworkInfo != null) { // connected to the internet
                if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                } else return activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            }
        }
        return false;
    }
    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
