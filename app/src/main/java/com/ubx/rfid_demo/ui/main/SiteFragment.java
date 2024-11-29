package com.ubx.rfid_demo.ui.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.ubx.rfid_demo.R;

public class SiteFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflar el diseño del fragmento
        View view = inflater.inflate(R.layout.fragment_site, container, false);

        // Inicializar el TabLayout y ViewPager
        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        ViewPager viewPager = view.findViewById(R.id.view_pager);

        // Configurar el adaptador del ViewPager
        SitePagerAdapter adapter = new SitePagerAdapter(getChildFragmentManager());
        adapter.addFragment(new EmpresaFragment(), "Empresa");
        adapter.addFragment(new LocalFragment(), "Local");

        // Asociar el adaptador con el ViewPager
        viewPager.setAdapter(adapter);

        // Asociar el ViewPager con el TabLayout
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }
}
