package com.sargent.mark.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.sargent.mark.todolist.data.Contract;
import com.sargent.mark.todolist.data.ToDoItem;

import java.util.ArrayList;

/**
 * Created by mark on 7/4/17.
 */

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.ItemHolder> {

    private Cursor cursor;
    private ItemClickListener listener;
    private String TAG = "todolistadapter";
    Context context;
    SQLiteDatabase db;

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item, parent, false);
        ItemHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemHolder holder, int position) {
        holder.bind(holder, position);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public interface ItemClickListener {
        void onItemClick(int pos, String description, String duedate, long id);
    }

    public ToDoListAdapter(Cursor cursor, ItemClickListener listener, SQLiteDatabase db) {
        this.cursor = cursor;
        this.listener = listener;
        this.db=db;
    }

    public void swapCursor(Cursor newCursor){
        if (cursor != null) cursor.close();
        cursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView due;
        String duedate;
        String description;
        //created variables for spinner and checkbox
        CheckBox descr;
        Spinner options;
        String optionSelected;
        int done;

        long id;


        ItemHolder(View view) {
            super(view);
            //getting spinner and checkbox object
            descr = (CheckBox) view.findViewById(R.id.description);
            due = (TextView) view.findViewById(R.id.dueDate);
            options = (Spinner) view.findViewById(R.id.item_spinner);
            view.setOnClickListener(this);
        }

        public void bind(ItemHolder holder, int pos) {
            cursor.moveToPosition(pos);
            id = cursor.getLong(cursor.getColumnIndex(Contract.TABLE_TODO._ID));
            Log.d(TAG, "deleting id: " + id);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,R.array.category_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            options.setAdapter(adapter);
            options.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id2) {

                    ContentValues cv = new ContentValues();
                    cv.put(Contract.TABLE_TODO.COLUMN_NAME_OPTION, parent.getItemAtPosition(position).toString());
                    db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            descr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ContentValues cv = new ContentValues();
                    cv.put(Contract.TABLE_TODO.COLUMN_NAME_DONE, isChecked);
                    db.update(Contract.TABLE_TODO.TABLE_NAME, cv, Contract.TABLE_TODO._ID + "=" + id, null);
                }
            });
            duedate = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DUE_DATE));
            description = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DESCRIPTION));
            //getting checkbox and option selected in a variable
            done = cursor.getInt(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_DONE));
            optionSelected = cursor.getString(cursor.getColumnIndex(Contract.TABLE_TODO.COLUMN_NAME_OPTION));
            descr.setText(description);
            due.setText(duedate);
            options.setSelection(adapter.getPosition(optionSelected));
            descr.setChecked(done>0?true:false);

            holder.itemView.setTag(id);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            listener.onItemClick(pos, description, duedate, id);
        }
    }

}
