package remotedoorway.byteme.com.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import remotedoorway.byteme.com.R;
import remotedoorway.byteme.com.models.Doors;

public class DoorListFragment extends Fragment {


    List<Doors> OthersDoorsList =new ArrayList<Doors>();
    List<Doors> OwnerDoorsList =new ArrayList<Doors>();

    ListView ownerlistview,otherlistview;
    ArrayAdapter<Doors> owneradaptor,otheradaptor;


    public DoorListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        populateFriendRequets();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doorlist, container, false);

        owneradaptor = new OwnerDoorAdaptor();
        otheradaptor = new OtherDoorAdaptor();

        ownerlistview = (ListView) view.findViewById(R.id.lv_fragment_doorlist_ownerdoorlist);
        otherlistview = (ListView) view.findViewById(R.id.lv_fragment_doorlist_otherdoorlist);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        ownerlistview.setAdapter(owneradaptor);
        otherlistview.setAdapter(otheradaptor);
    }

    private void populateFriendRequets()
    {
        final String userid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        DatabaseReference Doors = FirebaseDatabase.getInstance().getReference().child("UserInfo").child(userid).child("Doors");

        // now lets get all his friends id
        Doors.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // now seprating DataSnapshot according Others and Owner

                DataSnapshot DSOthersDoors = dataSnapshot.child("Others");
                DataSnapshot DSOwnerDoors = dataSnapshot.child("Owner");
                OwnerDoorsList.clear();
                OthersDoorsList.clear();

                //Log.v("W got:",dataSnapshot.child("Owner").toString());
                for (DataSnapshot doorRows : DSOthersDoors.getChildren()) {
                    final Doors doors=doorRows.getValue(Doors.class);
                    doors.setDoorId(doorRows.getKey());

                    //now let's find owner's name
                    DatabaseReference drowner=FirebaseDatabase.getInstance().getReference().child("UserInfo").child(doorRows.child("OwnerId").getValue().toString()).child("FullName");
                    drowner.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Log.v("Actually I got", "" + dataSnapshot.getValue());
                            String ownername = "" + dataSnapshot.getValue();
                            if(!ownername.equals("null"))
                                doors.setOwnerName(ownername);
                            else
                                doors.setOwnerName("Your Friend");

                            OthersDoorsList.add(doors);
                            Log.v("Others got:",doors.toString());
                            otherlistview.setAdapter(otheradaptor);
                        }



                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }


                for (DataSnapshot doorRows : DSOwnerDoors.getChildren()) {
                    final Doors doors=doorRows.getValue(Doors.class);
                    doors.setDoorId(doorRows.getKey());
                    OwnerDoorsList.add(doors);
                    Log.v("Owners got:",doors.toString());
                }


                ownerlistview.setAdapter(owneradaptor);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private class OwnerDoorAdaptor extends ArrayAdapter<Doors>
    {
        public OwnerDoorAdaptor() {
            super(getActivity().getBaseContext(),R.layout.ownerdoorlistlistrowview, OwnerDoorsList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View itemview=convertView;
            if(itemview==null)
            {
                itemview=getActivity().getLayoutInflater().inflate(R.layout.ownerdoorlistlistrowview,parent,false);
            }


            final Doors currentDoor= OwnerDoorsList.get(position);
            TextView tvdoorname=(TextView) itemview.findViewById(R.id.tvownerdoorlistdoorname);
            tvdoorname.setText(currentDoor.getDoorName());
            return itemview;
        }
    }



    private class OtherDoorAdaptor extends ArrayAdapter<Doors>
    {
        public OtherDoorAdaptor() {
            super(getActivity().getBaseContext(),R.layout.otherdoorlistlistrowview, OthersDoorsList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View itemview=convertView;
            if(itemview==null)
            {
                itemview=getActivity().getLayoutInflater().inflate(R.layout.otherdoorlistlistrowview,parent,false);
            }


            final Doors currentDoor= OthersDoorsList.get(position);
            TextView tvdoorname=(TextView) itemview.findViewById(R.id.tvotherdoorlistdoorname);
            TextView tvownermane=(TextView) itemview.findViewById(R.id.tvotherdoorlistownername);
            tvdoorname.setText(currentDoor.getDoorName());
            tvownermane.setText("Shared by " + currentDoor.getOwnerName());
            return itemview;
        }
    }


}
