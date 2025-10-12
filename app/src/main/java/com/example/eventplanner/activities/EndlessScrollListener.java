package com.example.eventplanner.activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    private final LinearLayoutManager lm;
    private boolean loading = false;
    private boolean lastPage = false;

    public EndlessScrollListener(LinearLayoutManager lm){
        this.lm = lm;
    }

    public void setLoading(boolean v){ this.loading = v; }
    public void setLastPage(boolean v){ this.lastPage = v; }

    @Override
    public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
        if (dy <= 0 || loading || lastPage) return;
        int lastVisible = lm.findLastVisibleItemPosition();
        int total = lm.getItemCount();
        if (lastVisible >= total - 3) {
            onLoadMore();
        }
    }

    public abstract void onLoadMore();
}
