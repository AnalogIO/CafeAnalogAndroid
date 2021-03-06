/*
 * Copyright 2016 Analog IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dk.cafeanalog;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CafeAnalogAdapter extends Adapter<CafeAnalogAdapter.TextViewHolder> {
    private final String[] mMenuItems;
    private final OnItemClickListener mListener;

    public CafeAnalogAdapter(String[] menuItems, OnItemClickListener listener) {
        mMenuItems = menuItems;
        mListener = listener;
    }

    @Override
    public TextViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item, parent, false);

        return new TextViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final TextViewHolder holder, int position) {
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClick(holder.getAdapterPosition());
            }
        });
        holder.setText(mMenuItems[position]);
    }

    @Override
    public int getItemCount() {
        return mMenuItems.length;
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public class TextViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public TextViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
        }

        public void setText(String text) {
            textView.setText(text);
        }
    }
}
