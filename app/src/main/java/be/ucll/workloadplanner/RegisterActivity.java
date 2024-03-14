package be.ucll.workloadplanner;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

// Scherm om een nieuwe gebruiker toe te voegen
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private EditText phoneNumberEditText, firstNameEditText, lastNameEditText, passWordEditText, emailEditText;
    private CheckBox agreeCheckBox;
    private Spinner roleSpinner;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        phoneNumberEditText = findViewById(R.id.phonenumber);
        emailEditText = findViewById(R.id.email);
        passWordEditText = findViewById(R.id.password);
        firstNameEditText = findViewById(R.id.firstname);
        lastNameEditText = findViewById(R.id.lastname);
        roleSpinner = findViewById(R.id.role);
        agreeCheckBox = findViewById(R.id.agree);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    // Methode om nieuwe gebruiker toe te voegen
    private void registerUser() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim().replaceAll("\\s", "");
        String email = emailEditText.getText().toString().trim();
        String passWord = passWordEditText.getText().toString().trim();
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString().trim();
        boolean agree = agreeCheckBox.isChecked();

        if (TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(email) || TextUtils.isEmpty(passWord) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!agree) {
            Toast.makeText(RegisterActivity.this, "Please agree to terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.setUserId(phoneNumber);
        user.setEmail(email);
        user.setPassword(passWord);
        user.setFirstname(firstName);
        user.setLastname(lastName);
        user.setRole(role);
        user.setAgree(agree);

        db.collection("users").document(phoneNumber).set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Log.e(TAG, "Error registering user", task.getException());
                            Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}