package  com.example.fitsoc.ui.login;

import static android.content.ContentValues.TAG;
import static android.view.View.*;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitsoc.NavigationActivity;
import com.example.fitsoc.R;
import com.example.fitsoc.databinding.ActivityRegisterBinding;
import com.example.fitsoc.data.model.RegisteredUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements OnClickListener{
    private LoginViewModel loginViewModel;
    private ActivityRegisterBinding bindingR;
    private FirebaseAuth mAuth;
    private EditText usernameRegisterEditText, passwordRegisterEditText;
    private Button registerEmailButton;
    private ProgressBar loadingProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindingR = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(bindingR.getRoot());
        mAuth = FirebaseAuth.getInstance();
        //Change button icon size
        Button button_register_email = (Button) findViewById(R.id.register_email);
        Drawable drawable_register_email = getResources().getDrawable(R.drawable.email_icon);
        drawable_register_email.setBounds(0, 0, 100, 100);
        button_register_email.setCompoundDrawables(drawable_register_email, null, null, null);

        Button button_register_wechat = (Button) findViewById(R.id.register_wechat);
        Drawable drawable_register_wechat = getResources().getDrawable(R.drawable.wechat_icon);
        drawable_register_wechat.setBounds(0, 0, 100, 100);
        button_register_wechat.setCompoundDrawables(drawable_register_wechat, null, null, null);

        //EditText usernameLoginEditText = (EditText) findViewById(R.id.username_register);
        //EditText passwordLoginEditText = (EditText) findViewById(R.id.password_register);

        //
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        usernameRegisterEditText = bindingR.usernameRegister;
        passwordRegisterEditText = bindingR.passwordRegister;
        //final EditText usernameLoginEditText = bindingR.usernameRegister;
        //final EditText passwordLoginEditText = bindingR.passwordRegister;
        registerEmailButton = bindingR.registerEmail;
//        final ProgressBar loadingProgressBar = bindingR.loading;
        loadingProgressBar = bindingR.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                registerEmailButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameRegisterEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordRegisterEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

//        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
//            @Override
//            public void onChanged(@Nullable LoginResult loginResult) {
//                if (loginResult == null) {
//                    return;
//                }
//                loadingProgressBar.setVisibility(GONE);
//                if (loginResult.getError() != null) {
//                    showLoginFailed(loginResult.getError());
//                }
//                if (loginResult.getSuccess() != null) {
//                    updateUiWithUser(loginResult.getSuccess());
//                }
//                setResult(Activity.RESULT_OK);
//
//                //Complete and destroy login activity once successful
//                finish();
//            }
//        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameRegisterEditText.getText().toString(),
                        passwordRegisterEditText.getText().toString());
            }
        };
        usernameRegisterEditText.addTextChangedListener(afterTextChangedListener);
        passwordRegisterEditText.addTextChangedListener(afterTextChangedListener);
        passwordRegisterEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Log.d(TAG, "click register button");
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //loginViewModel.login(usernameLoginEditText.getText().toString(),
                            //passwordLoginEditText.getText().toString());
                    Log.d(TAG, "press enter");
                }
                return false;
            }
        });

        button_register_email.setOnClickListener(this);
        /*
        registerEmailButton.setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                loadingProgressBar.setVisibility(View.VISIBLE);
//                loginViewModel.login(usernameLoginEditText.getText().toString(),
//                        passwordLoginEditText.getText().toString());
//                //同login page, 也需要修改
//                Intent intent = new Intent(RegisterActivity.this, NavigationActivity.class);
//                startActivity(intent);
                Log.d(TAG, "click register button");
                register();
                Log.d(TAG, "finish register");
            }
        });*/
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_email:
                register();
                break;
        }
    }

    public void register(){
        String username = usernameRegisterEditText.getText().toString();
        String password = passwordRegisterEditText.getText().toString();
        loadingProgressBar.setVisibility(VISIBLE);
        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference();
                        RegisteredUser newUser = new RegisteredUser(username);
                        //myRef.child("users").child(loggedInUser.getDisplayName()).setValue(loggedInUser);
                        myRef.child("users").child(newUser.getUserId().replace(".", ",")).setValue(newUser).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                // Register success, display a message to the user, then redirect to login page.
                                Log.d(TAG, "createUserWithEmail:success");
                                Toast.makeText(RegisterActivity.this, "Congratulations! You have registered successfully.", Toast.LENGTH_SHORT).show();
                                loadingProgressBar.setVisibility(GONE);
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                    } else{
                        // If register fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Email already registered! Please try another one.",
                                Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(GONE);
                    }
            });
    }

//    private void updateUiWithUser(LoggedInUserView model) {
//        String welcome = getString(R.string.welcome) ;
//        // TODO : initiate successful logged in experience
//        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
//    }
//
//    private void showLoginFailed(@StringRes Integer errorString) {
//        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
//    }

}
