package com.lyricslibrary.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.Holder> {
    public interface Listener {
        void onSongClick(Song song);
        void onSongLongPress(Song song);
    }

    private final Context context;
    private final Listener listener;
    private final List<Song> songs = new ArrayList<>();
    private long selectedId = -1;

    public SongAdapter(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
    }

    public List<Song> items() { return songs; }

    public void setSongs(List<Song> newSongs) {
        songs.clear();
        songs.addAll(newSongs);
        notifyDataSetChanged();
    }

    public void setSelectedId(long id) {
        selectedId = id;
        notifyDataSetChanged();
    }

    public void move(int from, int to) {
        if (from < 0 || to < 0 || from >= songs.size() || to >= songs.size()) return;
        Song s = songs.remove(from);
        songs.add(to, s);
        notifyItemMoved(from, to);
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(8), dp(12), dp(8));
        row.setMinimumHeight(dp(56));
        return new Holder(row);
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        Song s = songs.get(position);
        h.row.removeAllViews();
        h.row.setBackground(round(selectedId == s.id ? 0xFF344153 : 0xFF252F40, 12));
        LinearLayout textColumn = new LinearLayout(context);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        TextView title = new TextView(context);
        title.setText(s.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.RIGHT);
        title.setTextDirection(View.TEXT_DIRECTION_RTL);
        textColumn.addView(title, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout tags = new LinearLayout(context);
        tags.setOrientation(LinearLayout.HORIZONTAL);
        tags.setGravity(Gravity.RIGHT);
        if (!s.dastgah.trim().isEmpty()) tags.addView(tag(s.dastgah, colors(s.dastgah)[0], colors(s.dastgah)[1]));
        if (!s.gam.trim().isEmpty()) tags.addView(tag(s.gam, 0xFFE5E7EB, 0xFF4B5563));
        textColumn.addView(tags);
        h.row.addView(textColumn, new LinearLayout.LayoutParams(0, -2, 1));

        TextView handle = new TextView(context);
        handle.setText("⠿");
        handle.setTextSize(24);
        handle.setTextColor(0xFF9BAABD);
        handle.setGravity(Gravity.CENTER);
        h.row.addView(handle, new LinearLayout.LayoutParams(dp(34), dp(42)));

        h.itemView.setOnClickListener(v -> listener.onSongClick(s));
        h.itemView.setOnLongClickListener(v -> { listener.onSongLongPress(s); return true; });
    }

    private TextView tag(String value, int bg, int fg) {
        TextView t = new TextView(context);
        t.setText(value);
        t.setTextSize(10);
        t.setTextColor(fg);
        t.setPadding(dp(6), dp(3), dp(6), dp(3));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(0, dp(4), dp(5), 0);
        t.setLayoutParams(lp);
        t.setBackground(round(bg, 6));
        return t;
    }

    private int[] colors(String name) {
        switch (name) {
            case "شور": return new int[]{0xFFFEE2E2,0xFFB91C1C};
            case "سه‌گاه": return new int[]{0xFFDCFCE7,0xFF15803D};
            case "ماهور": return new int[]{0xFFDBEAFE,0xFF1D4ED8};
            case "اصفهان": return new int[]{0xFFFEF9C3,0xFFA16207};
            case "راست": return new int[]{0xFFFFEDD5,0xFFC2410C};
            case "نوا": return new int[]{0xFFFCE7F3,0xFFBE185D};
            case "چهارگاه": return new int[]{0xFFCFFAFE,0xFF155E75};
            case "همایون": return new int[]{0xFFE5E7EB,0xFF374151};
            case "راست‌پنجگاه": return new int[]{0xFFD9F99D,0xFF365314};
            case "کرد": return new int[]{0xFFF3E8FF,0xFF7E22CE};
            case "صبا": return new int[]{0xFFFEE2EF,0xFFB91C6A};
            default: return new int[]{0xFFE5E7EB,0xFF4B5563};
        }
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }
    private int dp(int n) { return (int)(n * context.getResources().getDisplayMetrics().density + 0.5f); }
    @Override public int getItemCount() { return songs.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        LinearLayout row;
        Holder(LinearLayout view) { super(view); row = view; }
    }
}
