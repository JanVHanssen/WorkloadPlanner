package be.ucll.workloadplanner;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fragment voor het aanpassen van een ticket door een member
public class UpdateTicketRestrictedFragment extends Fragment {

    private EditText titleEditText, infoEditText;
    private CheckBox finishedCheckBox;
    private Button updateButton;
    private Spinner userSpinner;
    private List<User> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_ticket_restricted, container, false);

        titleEditText = view.findViewById(R.id.title);
        infoEditText = view.findViewById(R.id.info);
        userSpinner = view.findViewById(R.id.user);
        finishedCheckBox = view.findViewById(R.id.finishedCheckBox);
        updateButton = view.findViewById(R.id.updateButton);

        updateButton.setOnClickListener(v -> updateTicketRestricted());

        Bundle args = getArguments();
        if (args != null) {
            titleEditText.setText(args.getString("title", ""));
            infoEditText.setText(args.getString("info", ""));

            retrieveUsersFromDatabase();

            finishedCheckBox.setChecked(args.getBoolean("finished", false));
        }

        titleEditText.setEnabled(false);
        infoEditText.setEnabled(false);

        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = userList.get(position);
                String selectedUserId = selectedUser.getUserId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    // Methode om een ticket te updaten door een member
    private void updateTicketRestricted() {
        boolean updatedFinished = finishedCheckBox.isChecked();

        Bundle args = getArguments();
        if (args != null) {
            String ticketId = args.getString("ticketId");

            User selectedUser = userList.get(userSpinner.getSelectedItemPosition());

            Map<String, Object> ticketData = new HashMap<>();
            ticketData.put("finished", updatedFinished);
            ticketData.put("user", selectedUser);

            FirebaseFirestore.getInstance()
                    .collection("tickets")
                    .document(ticketId)
                    .update(ticketData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(requireContext(), "Ticket updated successfully!", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(requireView()).navigate(R.id.action_updateTicketRestricted_to_tickets);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error updating ticket", e);
                        }
                    });
        }
    }

    // Lijst met users ophalen en in de spinner zetten, samen met een 'nobody' user
    private void retrieveUsersFromDatabase() {
        userList = new ArrayList<>();

        FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String firstName = documentSnapshot.getString("firstname");
                        String lastName = documentSnapshot.getString("lastname");

                        User user = new User();
                        user.setFirstname(firstName);
                        user.setLastname(lastName);

                        userList.add(user);
                    }

                    // De nobody optie toevoegen
                    User nobodyUser = new User();
                    nobodyUser.setUserId("none");
                    nobodyUser.setFirstname("Nobody");
                    nobodyUser.setLastname("Unassigned");
                    userList.add(0, nobodyUser);

                    List<String> userNames = new ArrayList<>();
                    for (User user : userList) {
                        userNames.add(user.getFirstname() + " " + user.getLastname());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, userNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    userSpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching users", e);
                });
    }
}