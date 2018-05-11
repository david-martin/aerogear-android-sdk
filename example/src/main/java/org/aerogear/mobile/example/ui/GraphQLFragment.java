package org.aerogear.mobile.example.ui;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.normalized.sql.ApolloSqlHelper;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.fetcher.ApolloResponseFetchers;
import com.apollographql.apollo.subscription.WebSocketSubscriptionTransport;
import com.github.nitrico.lastadapter.LastAdapter;

import android.databinding.ObservableArrayList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import org.aerogear.mobile.example.BR;
import org.aerogear.mobile.example.ListHeroesQuery;
import org.aerogear.mobile.example.R;
import org.aerogear.mobile.example.type.Episode;

import com.apollographql.apollo.ApolloClient;

import javax.annotation.Nonnull;

import butterknife.BindView;
import okhttp3.OkHttpClient;

public class GraphQLFragment extends BaseFragment {

    private static final String BASE_URL = "http://192.168.42.1:8080/graphql/";
    private static final String SUBSCRIPTION_BASE_URL = "ws://192.168.42.1:8080/subscriptions";
    private static final String SQL_CACHE_NAME = "starwarsdb";
    private ApolloClient apolloClient;

    private static final String TAG = "GraphQLFragment";

    @BindView(R.id.heroList)
    RecyclerView heroList;

    private ObservableArrayList<ListHeroesQuery.Hero> heros = new ObservableArrayList<>();

    @Override
    int getLayoutResId() {
        return R.layout.fragment_graphql;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .build();
        ApolloSqlHelper apolloSqlHelper = new ApolloSqlHelper(this.getContext(), SQL_CACHE_NAME);
//        NormalizedCacheFactory normalizedCacheFactory = new LruNormalizedCacheFactory(EvictionPolicy.NO_EVICTION)
//            .chain(new SqlNormalizedCacheFactory(apolloSqlHelper));
//
//        CacheKeyResolver cacheKeyResolver = new CacheKeyResolver() {
//            @Nonnull @Override
//            public CacheKey fromFieldRecordSet(@Nonnull ResponseField field, @Nonnull Map<String, Object> recordSet) {
//                String typeName = (String) recordSet.get("__typename");
//                if ("User".equals(typeName)) {
//                    String userKey = typeName + "." + recordSet.get("login");
//                    return CacheKey.from(userKey);
//                }
//                if (recordSet.containsKey("id")) {
//                    String typeNameAndIDKey = recordSet.get("__typename") + "." + recordSet.get("id");
//                    return CacheKey.from(typeNameAndIDKey);
//                }
//                return CacheKey.NO_KEY;
//            }
//
//            // Use this resolver to customize the key for fields with variables: eg entry(repoFullName: $repoFullName).
//            // This is useful if you want to make query to be able to resolved, even if it has never been run before.
//            @Nonnull @Override
//            public CacheKey fromFieldArguments(@Nonnull ResponseField field, @Nonnull Operation.Variables variables) {
//                return CacheKey.NO_KEY;
//            }
//        };

        apolloClient = ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(okHttpClient)
            //.normalizedCache(normalizedCacheFactory, cacheKeyResolver)
            .subscriptionTransportFactory(new WebSocketSubscriptionTransport.Factory(SUBSCRIPTION_BASE_URL, okHttpClient))
            .build();

        ApolloCall<ListHeroesQuery.Data> listHeroesQuery = apolloClient
            .query(new ListHeroesQuery(Episode.NEWHOPE))
            .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY);

        listHeroesQuery.enqueue(new ApolloCall.Callback<ListHeroesQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListHeroesQuery.Data> response) {
                Log.d(TAG, "onResponse" + response.data().toString());
                GraphQLFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        heros.add(response.data().hero());
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e(TAG, "onFailure", e);
            }
        });


        heroList.setLayoutManager(new LinearLayoutManager(getContext()));

        new LastAdapter(heros, BR.hero).map(ListHeroesQuery.Hero.class, R.layout.item_graphql).into(heroList);
    }

}
