package com.test.networkvulnerablilitycheck.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.networkvulnerablilitycheck.R;

public class MainListAdapter extends ArrayAdapter<String>{
    private final Activity context;
    private final String[] date;
    private final String[] router;
    private final String[] amount;
    public MainListAdapter(Activity context,
                      String[] date, String[] router,String[] amount) {
        super(context, R.layout.main_listview, date);
        this.context = context;
        this.date = date;
        this.router = router;
        this.amount = amount;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.main_listview, null, true);
        TextView datetxt = (TextView) rowView.findViewById(R.id.date);
        TextView routertxt = (TextView) rowView.findViewById(R.id.router);
        TextView amounttxt = (TextView) rowView.findViewById(R.id.amount);
        datetxt.setText(date[position]);
        routertxt.setText(router[position]);
        amounttxt.setText(amount[position]);
        return rowView;
    }
}