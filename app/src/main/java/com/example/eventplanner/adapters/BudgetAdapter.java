package com.example.eventplanner.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventplanner.R;
import com.example.eventplanner.dto.BudgetItemDTO;
import com.example.eventplanner.dto.CategoryDTO;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    
    private List<BudgetItemDTO> budgetItems;
    private List<CategoryDTO> availableCategories;
    private OnBudgetItemClickListener listener;
    
    public interface OnBudgetItemClickListener {
        void onAmountChanged(int position, Double amount);
        void onCategoryChanged(int position, Long categoryId);
        void onRemoveClick(int position);
    }
    
    public BudgetAdapter(List<BudgetItemDTO> budgetItems, List<CategoryDTO> availableCategories, OnBudgetItemClickListener listener) {
        this.budgetItems = budgetItems;
        this.availableCategories = availableCategories;
        this.listener = listener;
    }
    
    public void updateBudgetItems(List<BudgetItemDTO> newBudgetItems) {
        this.budgetItems = newBudgetItems;
        notifyDataSetChanged();
    }
    
    public void updateAvailableCategories(List<CategoryDTO> newCategories) {
        this.availableCategories = newCategories;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        BudgetItemDTO item = budgetItems.get(position);
        holder.bind(item, position);
    }
    
    @Override
    public int getItemCount() {
        return budgetItems.size();
    }
    
    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private Spinner spinnerCategory;
        private EditText etAmount;
        private LinearLayout layoutPurchaseInfo;
        private TextView tvPurchaseInfo;
        private MaterialButton btnRemove;
        
        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            spinnerCategory = itemView.findViewById(R.id.spinnerCategory);
            etAmount = itemView.findViewById(R.id.etAmount);
            layoutPurchaseInfo = itemView.findViewById(R.id.layoutPurchaseInfo);
            tvPurchaseInfo = itemView.findViewById(R.id.tvPurchaseInfo);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
        
        public void bind(BudgetItemDTO item, int position) {
            // Setup category spinner
            setupCategorySpinner(item, position);
            
            // Setup amount input
            if (item.getAmount() != null) {
                etAmount.setText(item.getAmount().toString());
            } else {
                etAmount.setText("");
            }
            
            // Add TextWatcher for real-time updates
            etAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        String amountText = s.toString().trim();
                        Double amount = amountText.isEmpty() ? 0.0 : Double.parseDouble(amountText);
                        if (listener != null) {
                            listener.onAmountChanged(position, amount);
                        }
                    } catch (NumberFormatException e) {
                        // Don't update if invalid number while typing
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            etAmount.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    try {
                        String amountText = etAmount.getText().toString().trim();
                        Double amount = amountText.isEmpty() ? 0.0 : Double.parseDouble(amountText);
                        if (listener != null) {
                            listener.onAmountChanged(position, amount);
                        }
                    } catch (NumberFormatException e) {
                        etAmount.setText("0");
                        if (listener != null) {
                            listener.onAmountChanged(position, 0.0);
                        }
                    }
                }
            });
            
            // Show purchase/reservation info if exists
            if (item.getPurchaseId() != null || item.getReservationId() != null) {
                layoutPurchaseInfo.setVisibility(View.VISIBLE);
                String info = "";
                if (item.getPurchaseId() != null) {
                    info += "Purchase ID: " + item.getPurchaseId();
                }
                if (item.getReservationId() != null) {
                    if (!info.isEmpty()) info += ", ";
                    info += "Reservation ID: " + item.getReservationId();
                }
                tvPurchaseInfo.setText(info);
            } else {
                layoutPurchaseInfo.setVisibility(View.GONE);
            }
            
            // Setup buttons
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveClick(position);
                }
            });
            
            // Disable remove button if item has purchases or reservations
            if (item.getPurchaseId() != null || item.getReservationId() != null) {
                btnRemove.setEnabled(false);
                btnRemove.setText(itemView.getContext().getString(R.string.cannot_remove));
            } else {
                btnRemove.setEnabled(true);
                btnRemove.setText(itemView.getContext().getString(R.string.remove));
            }
        }
        
        private void setupCategorySpinner(BudgetItemDTO item, int position) {
            if (availableCategories == null) {
                return;
            }
            
            // Create list of available categories for this item
            List<CategoryDTO> availableForThisItem = getAvailableCategoriesForItem(item, position);
            
            // Create adapter for spinner
            List<String> categoryNames = new ArrayList<>();
            List<Long> categoryIds = new ArrayList<>();
            
            categoryNames.add("-- " + itemView.getContext().getString(R.string.select_category) + " --");
            categoryIds.add(null);
            
            for (CategoryDTO category : availableForThisItem) {
                categoryNames.add(category.getName());
                categoryIds.add(category.getId());
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(), 
                android.R.layout.simple_spinner_item, categoryNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategory.setAdapter(adapter);
            
            // Set selected category
            if (item.getCategoryId() != null) {
                int selectedIndex = categoryIds.indexOf(item.getCategoryId());
                if (selectedIndex >= 0) {
                    spinnerCategory.setSelection(selectedIndex);
                }
            }
            
            // Setup listener
            spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int pos, long id) {
                    Long selectedCategoryId = pos > 0 ? categoryIds.get(pos) : null;
                    if (listener != null) {
                        listener.onCategoryChanged(position, selectedCategoryId);
                    }
                }
                
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    // Do nothing
                }
            });
        }
        
        private List<CategoryDTO> getAvailableCategoriesForItem(BudgetItemDTO currentItem, int currentPosition) {
            List<CategoryDTO> available = new ArrayList<>();
            
            // Get currently selected category IDs from other items
            List<Long> selectedIds = new ArrayList<>();
            for (int i = 0; i < budgetItems.size(); i++) {
                if (i != currentPosition && budgetItems.get(i).getCategoryId() != null) {
                    selectedIds.add(budgetItems.get(i).getCategoryId());
                }
            }
            
            // Add categories that are not selected by other items or are selected by current item
            for (CategoryDTO category : availableCategories) {
                if (category.getId().equals(currentItem.getCategoryId()) || !selectedIds.contains(category.getId())) {
                    available.add(category);
                }
            }
            
            return available;
        }
    }
}
