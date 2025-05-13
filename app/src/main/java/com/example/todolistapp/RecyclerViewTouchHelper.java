package com.example.todolistapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistapp.Adapter.ToDoAdapter;
import com.google.android.material.transition.MaterialElevationScale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

// Class that handles swipe gestures on RecyclerView items
public class RecyclerViewTouchHelper extends ItemTouchHelper.SimpleCallback {

    ToDoAdapter adapter; // Adapter to perform actions on

    // Constructor: enable swipe left and right
    public RecyclerViewTouchHelper(ToDoAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    // We are not supporting move actions (drag & drop), only swipe
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    // Handle swipe events
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();

        // Swipe to the right: ask to delete the task
        if (direction == ItemTouchHelper.RIGHT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Delete Task");
            builder.setMessage("Are you sure you want to delete this task?");

            // If user confirms, delete the task
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.deleteTask(position);
                }
            });

            // If user cancels, refresh the item (undo swipe)
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adapter.notifyItemChanged(position);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            // Swipe to the left: edit the task
            adapter.editTask(position);
        }
    }

    // Decorate the swipe action with background color and icons
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {

        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(adapter.getContext(), R.color.green)) // Green for edit
                .addSwipeLeftActionIcon(R.drawable.edit) // Edit icon
                .addSwipeRightBackgroundColor(ContextCompat.getColor(adapter.getContext(), R.color.red)) // Red for delete
                .addSwipeRightActionIcon(R.drawable.delete) // Delete icon
                .create()
                .decorate();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
