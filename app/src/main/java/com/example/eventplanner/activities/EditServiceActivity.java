package com.example.eventplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eventplanner.R;

public class EditServiceActivity extends AppCompatActivity {

//    View[] eventTypes = new View[]{};
    private boolean[] selectedStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_service);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View eventType1 = findViewById(R.id.eventTypeEdit1);
        View eventType2 = findViewById(R.id.eventTypeEdit2);
        View eventType3 = findViewById(R.id.eventTypeEdit3);
        View eventType4 = findViewById(R.id.eventTypeEdit4);
        View eventType5 = findViewById(R.id.eventTypeEdit5);
        View[] eventTypes = new View[]{eventType1, eventType2, eventType3, eventType4, eventType5};

        selectedStates = new boolean[eventTypes.length];
        selectedStates[1] = true;
        selectedStates[3] = true;

        View category = findViewById(R.id.category1);
        category.setBackgroundResource(R.drawable.border_background_purple);
        TextView selectedServiceName = category.findViewById(R.id.service_name);
        selectedServiceName.setTextColor(ContextCompat.getColor(this, R.color.white));
        TextView selectedServiceDescription = category.findViewById(R.id.service_description);
        selectedServiceDescription.setTextColor(ContextCompat.getColor(this, R.color.white));

        for (int i = 0; i < eventTypes.length; i++) {
            final int index = i;
            updateEventTypeStyle(index);
            eventTypes[i].setOnClickListener(v -> toggleSelection(index));
        }

        RadioButton visibleBtn = findViewById(R.id.not_visible);
        visibleBtn.setChecked(true);

        RadioButton availableBtn = findViewById(R.id.available);
        availableBtn.setChecked(true);

        RadioButton reservationTypeBtn = findViewById(R.id.manual);
        reservationTypeBtn.setChecked(true);

        Button btnAddNew = findViewById(R.id.edit);
        btnAddNew.setOnClickListener(v -> {
            Toast.makeText(EditServiceActivity.this, "Service successfully edited!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(EditServiceActivity.this, ServiceActivity.class);
            startActivity(intent);
        });

        Button btnCancel = findViewById(R.id.cancel);
        btnCancel.setOnClickListener(v -> {
            Intent intent = new Intent(EditServiceActivity.this, ServiceActivity.class);
            startActivity(intent);
        });
    }

    private void toggleSelection(int index) {
        selectedStates[index] = !selectedStates[index]; // Promeni stanje
        updateEventTypeStyle(index); // Ažuriraj stil stavke
    }

    private void updateEventTypeStyle(int index) {
        View eventType1 = findViewById(R.id.eventTypeEdit1);
        View eventType2 = findViewById(R.id.eventTypeEdit2);
        View eventType3 = findViewById(R.id.eventTypeEdit3);
        View eventType4 = findViewById(R.id.eventTypeEdit4);
        View eventType5 = findViewById(R.id.eventTypeEdit5);
        View[] eventTypes = new View[]{eventType1, eventType2, eventType3, eventType4, eventType5};

        if (selectedStates[index]) {
            eventTypes[index].setBackgroundResource(R.drawable.border_background_purple);
            TextView eventTypeName = eventTypes[index].findViewById(R.id.name_of_event_type);
            eventTypeName.setTextColor(ContextCompat.getColor(this, R.color.white));
            TextView eventTypeDescription = eventTypes[index].findViewById(R.id.event_type_description);
            eventTypeDescription.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            eventTypes[index].setBackgroundResource(R.drawable.border_background_black);
            TextView eventTypeName = eventTypes[index].findViewById(R.id.name_of_event_type);
            eventTypeName.setTextColor(ContextCompat.getColor(this, R.color.black));
            TextView eventTypeDescription = eventTypes[index].findViewById(R.id.event_type_description);
            eventTypeDescription.setTextColor(ContextCompat.getColor(this, R.color.black));
        }
    }
}
