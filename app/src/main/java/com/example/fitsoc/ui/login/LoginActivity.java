package  com.example.fitsoc.ui.login;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitsoc.NavigationActivity;
import com.example.fitsoc.R;
import com.example.fitsoc.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private EditText usernameLoginEditText, passwordLoginEditText;
    private Button loginButton;
    private FirebaseAuth mAuth;
    private ProgressBar loadingProgressBar;

    private String userID;
    private String password;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuth = FirebaseAuth.getInstance();

        if(readFromSharedPref()){
            final EditText usernameLoginEditText = binding.usernameLogin;
            final EditText passwordLoginEditText = binding.passwordLogin;
            usernameLoginEditText.setText(userID);
            passwordLoginEditText.setText(password);
            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
            startActivity(intent);
        }

        Button button_login_email = (Button) findViewById(R.id.login_email);
        Drawable drawable_login_email = getResources().getDrawable(R.drawable.email_icon);
        drawable_login_email.setBounds(0, 0, 100, 100);
        button_login_email.setCompoundDrawables(drawable_login_email, null, null, null);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        usernameLoginEditText = binding.usernameLogin;
        passwordLoginEditText = binding.passwordLogin;
        loginButton = binding.loginEmail;
        loadingProgressBar = binding.loading;

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameLoginEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordLoginEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

//        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
//            @Override
//            public void onChanged(@Nullable LoginResult loginResult) {
//                if (loginResult == null) {
//                    return;
//                }
//                loadingProgressBar.setVisibility(View.GONE);
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
                loginViewModel.loginDataChanged(usernameLoginEditText.getText().toString(),
                        passwordLoginEditText.getText().toString());
            }
        };
        usernameLoginEditText.addTextChangedListener(afterTextChangedListener);
        passwordLoginEditText.addTextChangedListener(afterTextChangedListener);
        passwordLoginEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameLoginEditText.getText().toString(),
                            passwordLoginEditText.getText().toString());
                }
                return false;
            }
        });

//        loginButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                loadingProgressBar.setVisibility(View.VISIBLE);
////                loginViewModel.login(usernameLoginEditText.getText().toString(),
////                        passwordLoginEditText.getText().toString());
////                //下面这个地方在有了database后修改，得成功才能进入
////                Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
////                startActivity(intent);
//                switch (v.getId()) {
//                    case R.id.login_email:
//                        login();
//                        break;
//                }
//            }
//        });
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_email:
                login();
                break;
        }
    }

    private void login() {
        String username = usernameLoginEditText.getText().toString();
        String password = passwordLoginEditText.getText().toString();
        loadingProgressBar.setVisibility(VISIBLE);
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //store user info into shared preference
                            writeToSharedPref(username,password);

                            // Login success, display a message to the user, then redirect to navigation page.
                            Log.d(TAG, "loginUserWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Congratulations! You have logged in successfully.", Toast.LENGTH_SHORT).show();
                            loadingProgressBar.setVisibility(GONE);
                            Intent intent = new Intent(LoginActivity.this, NavigationActivity.class);
                            startActivity(intent);
                        } else{
                            // If login fails, display a message to the user.
                            Log.w(TAG, "loginUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Failed to login! Please check your credentials.",
                                    Toast.LENGTH_SHORT).show();
                            loadingProgressBar.setVisibility(GONE);
                        }
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

    public void writeToSharedPref(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userID",binding.usernameLogin.getText().toString());
        editor.putString("password",binding.passwordLogin.getText().toString());
        editor.apply();
    }

    public void  writeToSharedPref(String userName, String password){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("userID",userName);
        editor.putString("password",password);
        editor.apply();
    }

    public boolean readFromSharedPref(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        userID = sharedPref.getString("userID","");
        password = sharedPref.getString("password","");
        if(!userID.isEmpty() && !password.isEmpty()){
            return true;
        }
        return  false;
    }
}