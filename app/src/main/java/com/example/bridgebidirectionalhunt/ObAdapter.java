
package com.example.bridgebidirectionalhunt;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

public class ObAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;

    public ObAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    private int titles[] = {
            R.string.onboarding_title_1,
            R.string.onboarding1_title_2,
    };

    private int descriptions[] = {
            R.string.onboarding1,
            R.string.onboarding2,
    };

    private int images[] ={
            R.drawable.onboarding1,
            R.drawable.onboarding2,
    };

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (ConstraintLayout) object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((ConstraintLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = layoutInflater.inflate(R.layout.page_ob,container,false);

        ImageView image = v.findViewById(R.id.image);
        TextView title,description;
        title = v.findViewById(R.id.title);
        description = v.findViewById(R.id.description);

        image.setImageResource(images[position]);
        title.setText(titles[position]);
        description.setText(descriptions[position]);

        container.addView(v);
        return v;
    }
}