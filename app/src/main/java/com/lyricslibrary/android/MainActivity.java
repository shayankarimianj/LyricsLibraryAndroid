package com.lyricslibrary.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements SongAdapter.Listener {
    private SongDatabase database;
    private SongAdapter adapter;
    private EditText search;
    private RecyclerView songList;
    private TextView count, selectedTitle, lyrics;
    private Button editButton, deleteButton, fontMinus, fontPlus, fontButton;
    private long currentId = -1;
    private String currentQuery = "";
    private int fontSize = 18;
    private String fontFamily = "sans";
    private SharedPreferences prefs;
    private int blue = 0xFF3987D8, dark = 0xFF202938, sidebar = 0xFF252F40;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(dark);
        getWindow().setNavigationBarColor(dark);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        database = new SongDatabase(this);
        prefs = getSharedPreferences("display", MODE_PRIVATE);
        fontSize = prefs.getInt("font_size", 18);
        fontFamily = prefs.getString("font_family", "sans");
        buildUi();
        refreshList();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12), dp(10), dp(12), dp(10));
        root.setBackgroundColor(dark);
        setContentView(root);

        TextView header = text("کتابخانه متن آهنگ‌ها", 21, Color.WHITE, true);
        header.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(42)));

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(14);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(0xFFB7C3D1);
        search.setHint("جستجوی نام، متن، دستگاه یا گام...");
        search.setPadding(dp(10), 0, dp(10), 0);
        search.setBackground(round(0xFF344153, 10));
        searchRow.addView(search, new LinearLayout.LayoutParams(0, dp(46), 1));
        Button add = button("+ آهنگ جدید", blue);
        LinearLayout.LayoutParams addLp = new LinearLayout.LayoutParams(-2, dp(46));
        addLp.setMargins(dp(7), 0, 0, 0);
        searchRow.addView(add, addLp);
        root.addView(searchRow);

        LinearLayout listHead = new LinearLayout(this);
        listHead.setGravity(Gravity.CENTER_VERTICAL);
        TextView listTitle = text("لیست آهنگ‌ها", 15, 0xFFE7EDF5, true);
        listHead.addView(listTitle, new LinearLayout.LayoutParams(0, dp(34), 1));
        count = text("۰ آهنگ", 12, 0xFF9BAABD, false);
        listHead.addView(count);
        root.addView(listHead);

        songList = new RecyclerView(this);
        songList.setLayoutManager(new LinearLayoutManager(this));
        songList.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        adapter = new SongAdapter(this, this);
        songList.setAdapter(adapter);
        songList.setBackground(round(sidebar, 12));
        songList.setPadding(dp(5), dp(4), dp(5), dp(4));
        songList.setClipToPadding(false);
        root.addView(songList, new LinearLayout.LayoutParams(-1, 0, 4.0f));

        TextView detailHeader = text("متن آهنگ انتخاب‌شده", 15, 0xFFE7EDF5, true);
        LinearLayout.LayoutParams dhp = new LinearLayout.LayoutParams(-1, dp(34));
        dhp.topMargin = dp(8);
        root.addView(detailHeader, dhp);

        LinearLayout detail = new LinearLayout(this);
        detail.setOrientation(LinearLayout.VERTICAL);
        detail.setPadding(dp(12), dp(10), dp(12), dp(10));
        detail.setBackground(round(Color.WHITE, 12));
        selectedTitle = text("یک آهنگ انتخاب کنید", 19, 0xFF0246A6, true);
        selectedTitle.setGravity(Gravity.RIGHT);
        detail.addView(selectedTitle, new LinearLayout.LayoutParams(-1, -2));

        ScrollView lyricScroll = new ScrollView(this);
        lyrics = text("", fontSize, 0xFF263448, false);
        lyrics.setGravity(Gravity.TOP | Gravity.RIGHT);
        lyrics.setTextDirection(View.TEXT_DIRECTION_RTL);
        lyrics.setLineSpacing(dp(4), 1.0f);
        lyricScroll.addView(lyrics);
        LinearLayout.LayoutParams lyricLp = new LinearLayout.LayoutParams(-1, 0, 1);
        lyricLp.topMargin = dp(6);
        detail.addView(lyricScroll, lyricLp);
        root.addView(detail, new LinearLayout.LayoutParams(-1, 0, 5.0f));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        fontMinus = button("−", 0xFFEDF2F8);
        fontMinus.setTextColor(0xFF344153);
        fontPlus = button("+", 0xFFEDF2F8);
        fontPlus.setTextColor(0xFF344153);
        fontButton = button("فونت", 0xFFEDF2F8);
        fontButton.setTextColor(0xFF344153);
        controls.addView(text("اندازه", 12, 0xFFE7EDF5, false));
        controls.addView(fontMinus, new LinearLayout.LayoutParams(dp(42), dp(40)));
        TextView sizeLabel = text(String.valueOf(fontSize), 13, Color.WHITE, true);
        controls.addView(sizeLabel);
        fontMinus.setOnClickListener(v -> { fontSize = Math.max(8, fontSize - 1); sizeLabel.setText(String.valueOf(fontSize)); applyFont(); });
        fontPlus.setOnClickListener(v -> { fontSize = Math.min(48, fontSize + 1); sizeLabel.setText(String.valueOf(fontSize)); applyFont(); });
        controls.addView(fontPlus, new LinearLayout.LayoutParams(dp(42), dp(40)));
        controls.addView(fontButton);
        View spacer = new View(this);
        controls.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));
        editButton = button("ویرایش", blue);
        deleteButton = button("حذف", 0xFFFCE8E8);
        deleteButton.setTextColor(0xFFC24141);
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        controls.addView(editButton);
        controls.addView(deleteButton);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(48));
        cp.topMargin = dp(5);
        root.addView(controls, cp);

        add.setOnClickListener(v -> addSong());
        editButton.setOnClickListener(v -> editSong());
        deleteButton.setOnClickListener(v -> deleteSong());
        fontButton.setOnClickListener(v -> chooseFont());
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) {
                currentQuery = s.toString();
                refreshList();
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override public boolean isLongPressDragEnabled() { return currentQuery.trim().isEmpty(); }
            @Override public boolean isItemViewSwipeEnabled() { return false; }
            @Override public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                int from = vh.getBindingAdapterPosition(), to = target.getBindingAdapterPosition();
                if (!currentQuery.trim().isEmpty()) return false;
                adapter.move(from, to);
                return true;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder vh, int direction) {}
            @Override public void clearView(RecyclerView rv, RecyclerView.ViewHolder vh) {
                super.clearView(rv, vh);
                if (currentQuery.trim().isEmpty()) database.saveOrder(adapter.items());
                refreshList();
            }
        });
        helper.attachToRecyclerView(songList);
        applyFont();
    }

    private void refreshList() {
        if (adapter == null || database == null) return;
        List<Song> all = database.allSongs();
        String q = normalize(currentQuery.trim());
        List<Song> filtered = new ArrayList<>();
        for (Song s : all) {
            String searchable = normalize(s.title + " " + s.lyrics + " " + s.dastgah + " " + s.gam);
            if (q.isEmpty() || searchable.contains(q)) filtered.add(s);
        }
        adapter.setSongs(filtered);
        count.setText(filtered.size() + " آهنگ" + (q.isEmpty() ? "" : " • برای تغییر ترتیب جستجو را پاک کنید"));
        adapter.setSelectedId(currentId);
        if (currentId != -1) showSong(database.getSong(currentId));
    }

    private String normalize(String s) {
        return Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFC)
                .replace('ي', 'ی').replace('ك', 'ک').replace("\u0640", "")
                .toLowerCase(Locale.ROOT);
    }

    @Override public void onSongClick(Song song) { showSong(song); }
    @Override public void onSongLongPress(Song song) {
        if (!currentQuery.trim().isEmpty()) toast("برای تغییر ترتیب، ابتدا جستجو را پاک کنید.");
        else toast("برای جابه‌جایی، انگشت را روی آهنگ نگه دارید و بکشید.");
    }

    private void showSong(Song song) {
        if (song == null) return;
        currentId = song.id;
        selectedTitle.setText(song.title + tagLine(song));
        lyrics.setText(song.lyrics);
        editButton.setEnabled(true);
        deleteButton.setEnabled(true);
        adapter.setSelectedId(currentId);
    }

    private String tagLine(Song s) {
        String tags = "";
        if (!s.dastgah.trim().isEmpty()) tags += "   • " + s.dastgah;
        if (!s.gam.trim().isEmpty()) tags += "   • " + s.gam;
        return tags;
    }

    private void addSong() {
        EditText title = field("نام آهنگ");
        new AlertDialog.Builder(this).setTitle("آهنگ جدید").setView(dialogWrap(title))
                .setPositiveButton("ایجاد", (d, w) -> {
                    String value = title.getText().toString().trim();
                    if (value.isEmpty()) { toast("نام آهنگ نمی‌تواند خالی باشد."); return; }
                    currentId = database.addSong(value);
                    search.setText("");
                    refreshList();
                    showSong(database.getSong(currentId));
                }).setNegativeButton("انصراف", null).show();
    }

    private void editSong() {
        Song song = database.getSong(currentId);
        if (song == null) return;
        LinearLayout form = new LinearLayout(this);
        form.setPadding(dp(18), dp(4), dp(18), dp(4));
        form.setOrientation(LinearLayout.VERTICAL);
        EditText title = field("نام آهنگ"); title.setText(song.title);
        EditText dastgah = field("دستگاه"); dastgah.setText(song.dastgah);
        EditText gam = field("گام"); gam.setText(song.gam);
        EditText lyric = field("متن آهنگ");
        lyric.setText(song.lyrics);
        lyric.setGravity(Gravity.TOP | Gravity.RIGHT);
        lyric.setMinLines(5);
        lyric.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        form.addView(title); form.addView(dastgah); form.addView(gam); form.addView(lyric, new LinearLayout.LayoutParams(-1, dp(180)));
        ScrollView scroller = new ScrollView(this); scroller.addView(form);
        new AlertDialog.Builder(this).setTitle("ویرایش آهنگ").setView(scroller)
                .setPositiveButton("ذخیره تغییرات", (d, w) -> {
                    String t = title.getText().toString().trim();
                    if (t.isEmpty()) { toast("نام آهنگ نمی‌تواند خالی باشد."); return; }
                    song.title = t;
                    song.dastgah = dastgah.getText().toString().trim();
                    song.gam = gam.getText().toString().trim();
                    song.lyrics = lyric.getText().toString();
                    database.updateSong(song);
                    refreshList();
                    showSong(database.getSong(currentId));
                    toast("تغییرات ذخیره شد.");
                }).setNegativeButton("انصراف", null).show();
    }

    private void deleteSong() {
        if (currentId == -1) return;
        new AlertDialog.Builder(this).setTitle("حذف آهنگ")
                .setMessage("آیا از حذف این آهنگ مطمئن هستید؟")
                .setPositiveButton("حذف", (d, w) -> {
                    database.deleteSong(currentId);
                    currentId = -1;
                    selectedTitle.setText("یک آهنگ انتخاب کنید");
                    lyrics.setText("");
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    refreshList();
                    toast("آهنگ حذف شد.");
                }).setNegativeButton("انصراف", null).show();
    }

    private void chooseFont() {
        String[] names = {"پیش‌فرض سیستم", "Serif", "Monospace", "Sans Serif"};
        String[] values = {"sans", "serif", "mono", "sans"};
        new AlertDialog.Builder(this).setTitle("انتخاب فونت")
                .setItems(names, (d, which) -> {
                    fontFamily = values[which];
                    prefs.edit().putString("font_family", fontFamily).apply();
                    applyFont();
                }).show();
    }

    private void applyFont() {
        Typeface typeface;
        switch (fontFamily) {
            case "serif": typeface = Typeface.SERIF; break;
            case "mono": typeface = Typeface.MONOSPACE; break;
            default: typeface = Typeface.DEFAULT;
        }
        lyrics.setTextSize(fontSize);
        lyrics.setTypeface(typeface);
        prefs.edit().putInt("font_size", fontSize).apply();
    }

    private EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(15);
        e.setSingleLine(false);
        e.setTextDirection(View.TEXT_DIRECTION_RTL);
        e.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        return e;
    }

    private View dialogWrap(View child) {
        LinearLayout box = new LinearLayout(this);
        box.setPadding(dp(20), dp(5), dp(20), dp(5));
        box.addView(child, new LinearLayout.LayoutParams(-1, dp(54)));
        return box;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);
        return t;
    }

    private Button button(String label, int color) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(12);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackground(round(color, 8));
        b.setPadding(dp(7), 0, dp(7), 0);
        return b;
    }

    private android.graphics.drawable.GradientDrawable round(int color, int radius) {
        android.graphics.drawable.GradientDrawable d = new android.graphics.drawable.GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    @Override protected void onDestroy() {
        if (database != null) database.close();
        super.onDestroy();
    }
}
