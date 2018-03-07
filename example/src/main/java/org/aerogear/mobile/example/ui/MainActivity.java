package org.aerogear.mobile.example.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.aerogear.mobile.auth.AuthService;
import org.aerogear.mobile.auth.configuration.AuthServiceConfiguration;
import org.aerogear.mobile.auth.user.UserPrincipal;
import org.aerogear.mobile.example.R;
import org.jboss.aerogear.android.core.Callback;
import org.jboss.aerogear.android.unifiedpush.PushRegistrar;
import org.jboss.aerogear.android.unifiedpush.RegistrarManager;
import org.jboss.aerogear.android.unifiedpush.fcm.AeroGearFCMPushConfiguration;

import java.net.URI;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity
                implements NavigationView.OnNavigationItemSelectedListener {

    private final String VARIANT_ID       = "f2a7fb40-9694-400c-a2ca-ee6c9fe791b4";
    private final String SECRET           = "b0150669-a40a-4c4d-b363-ca7f570ec479";
    private final String FCM_SENDER_ID    = "916837949223";
    private final String UNIFIED_PUSH_URL = "http://192.168.1.12:9999/ag-push/";

    private final String TAG = "debug";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RegistrarManager.config("register", AeroGearFCMPushConfiguration.class)
            .setPushServerURI(URI.create(UNIFIED_PUSH_URL))
            .setSenderId(FCM_SENDER_ID)
            .setVariantID(VARIANT_ID)
            .setSecret(SECRET)
            .asRegistrar();

        PushRegistrar registrar = RegistrarManager.getRegistrar("register");
        registrar.register(getApplicationContext(), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.i(TAG, "Registration Succeeded!");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        });

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        authService = (AuthService) mobileCore.getInstance(AuthService.class);
        AuthServiceConfiguration authServiceConfiguration =
                        new AuthServiceConfiguration.AuthConfigurationBuilder()
                                        .withRedirectUri("org.aerogear.mobile.example:/callback")
                                        .build();
        authService.init(getApplicationContext(), authServiceConfiguration);

        navigationView.setNavigationItemSelectedListener(this);

        navigateTo(new HomeFragment());
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AuthFragment.LOGIN_RESULT_CODE) {
            authService.handleAuthResult(data);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_http:
                navigateTo(new HttpFragment());
                break;
            case R.id.nav_auth:
                UserPrincipal currentUser = authService.currentUser();
                if (currentUser != null) {
                    navigateToAuthDetailsView(currentUser);
                } else {
                    navigateTo(new AuthFragment());
                }
                break;
            case R.id.nav_checks:
                navigateTo(new SecurityServiceFragment());
                break;
            default:
                navigateTo(new HomeFragment());
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void navigateTo(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void navigateToAuthDetailsView(UserPrincipal currentUser) {
        AuthDetailsFragment nextFragment = new AuthDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("currentUser", currentUser);
        nextFragment.setArguments(bundle);
        navigateTo(nextFragment);
    }

}
