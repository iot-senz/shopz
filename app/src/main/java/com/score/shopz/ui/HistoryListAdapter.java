package com.score.shopz.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.score.shopz.R;
import com.score.shopz.pojos.Payz;

import java.util.ArrayList;

/**
 * Display friend list
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class HistoryListAdapter extends BaseAdapter {

    private HistoryListActivity activity;
    private Typeface typeface;
    private ArrayList<Payz> payzList;

    /**
     * Initialize context variables
     *
     * @param activity friend list activity
     * @param payzList friend list
     */
    public HistoryListAdapter(HistoryListActivity activity, ArrayList<Payz> payzList) {
        this.activity = activity;
        this.payzList = payzList;
        typeface = Typeface.createFromAsset(activity.getAssets(), "fonts/vegur_2.otf");
    }

    /**
     * Get size of user list
     *
     * @return userList size
     */
    @Override
    public int getCount() {
        return payzList.size();
    }

    /**
     * Get specific item from user list
     *
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return payzList.get(i);
    }

    /**
     * Get user list item id
     *
     * @param i item index
     * @return current item id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Create list row view
     *
     * @param position index
     * @param view     current list item view
     * @param parent   parent
     * @return view
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        // A ViewHolder keeps references to children views to avoid unnecessary calls
        // to findViewById() on each row.
        final ViewHolder holder;
        final Payz payz = (Payz) getItem(position);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.history_list_row_layout, parent, false);
            holder = new ViewHolder();
            holder.iconText = (TextView) view.findViewById(R.id.icon_text);
            holder.name = (TextView) view.findViewById(R.id.history_list_row_layout_name);
            holder.amount = (TextView) view.findViewById(R.id.history_list_row_layout_amount);
            holder.iconText.setTypeface(typeface, Typeface.BOLD);
            holder.iconText.setTextColor(activity.getResources().getColor(R.color.white));
            holder.name.setTypeface(typeface, Typeface.NORMAL);
            holder.amount.setTypeface(typeface, Typeface.NORMAL);

            view.setTag(holder);
        } else {
            // get view holder back
            holder = (ViewHolder) view.getTag();
        }

        // bind text with view holder content view for efficient use
        holder.iconText.setText("$");
        holder.name.setText(payz.getTime());
        holder.amount.setText("$ " + payz.getAmount());
        //view.setBackgroundResource(R.drawable.more_layout_selector_normal);

        return view;
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView iconText;
        TextView name;
        TextView amount;
    }

}
