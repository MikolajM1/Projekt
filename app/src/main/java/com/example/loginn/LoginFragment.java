package com.example.loginn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.pd.chocobar.ChocoBar;

import java.util.Arrays;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private EditText editText1L;
    private EditText editText2L;
    private Button button1L;
    private Button button2L;
    int RC_SIGN_IN = 0;
    private GoogleSignInClient mGoogleSignInClient;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private OnFragmentInteractionListener mListener;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        v.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("GoogleOnClick", "clicked");
                signIn();
            }
        });

        button1L = v.findViewById(R.id.button1L);
        button2L = v.findViewById(R.id.button2L);
        editText1L = v.findViewById(R.id.editText1L);
        editText2L = v.findViewById(R.id.editText2L);

        button1L.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
               if(editText1L.getText().toString().equals("") && editText2L.getText().toString().equals("")){
                   //User didn't provide anything, notify them
                   Toast.makeText(getContext(), "Please provide credentials", LENGTH_SHORT).show();
               }else{
                   mAuth.fetchSignInMethodsForEmail(editText1L.getText().toString()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                       @Override
                       public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.getResult().getSignInMethods().isEmpty()){
                                Log.i("Check for email use", "not used at all");
                                hideKeyboard(getActivity());
                                ChocoBar.builder().setActivity(getActivity()).setActionText("Create an account")
                                        .setActionClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                register();
                                            }
                                        })
                                        .setText("Provided email is not linked to any existing account")
                                        .setDuration(ChocoBar.LENGTH_LONG)
                                        .build()
                                        .show();
                            }else{
                                if (task.getResult().getSignInMethods().get(0).equals("password")){
                                    logIn();
                                    Log.i("Check for email use", "used for email and password authentication");
                                }else{
                                    Log.i("Check for email use", "used for other auth methods");
                                    hideKeyboard(getActivity());
                                    ChocoBar.builder().setActivity(getActivity()).setActionText("Create an account")
                                            .setActionClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    register();
                                                }
                                            })
                                            .setText("Provided email is linked to a different authentication method")
                                            .setDuration(ChocoBar.LENGTH_LONG)
                                            .build()
                                            .show();
                                }
                            }
                       }
                   });
               }
            }
        });

        button2L.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i("onStart", "started");
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            Log.i("onStart",  "someone is logged in "+currentUser.getEmail());
            if (currentUser.isEmailVerified()){
                home();
            }else{
                hideKeyboard(getActivity());
                ChocoBar.builder().setActivity(getActivity()).setActionText("Verify the email address")
                        .setActionClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                sendEmailVerification();
                            }
                        })
                        .setText("This account's email address is not verified")
                        .setDuration(ChocoBar.LENGTH_LONG)
                        .build()
                        .show();
            }
        }else{
            Log.i("onStart", "no one is logged in");
        }
    }

    public void logIn(){
        mAuth.signInWithEmailAndPassword(editText1L.getText().toString(), editText2L.getText().toString())
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SIGN IN", "signInWithEmail:success");
                            user = mAuth.getCurrentUser();
                            if (IsEmailVerified()){
                                home();
                            }else{
                                hideKeyboard(getActivity());
                                ChocoBar.builder().setActivity(getActivity()).setActionText("Send verification email")
                                        .setActionClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                sendEmailVerification();
                                            }
                                        })
                                        .setText("This account's email address is not verified")
                                        .setDuration(ChocoBar.LENGTH_LONG)
                                        .build()
                                        .show();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SIGN IN", "signInWithEmail:failure", task.getException());
                            hideKeyboard(getActivity());
                            ChocoBar.builder().setActivity(getActivity()).setActionText("Reset your password")
                                    .setActionClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            final String email = editText1L.getText().toString();

                                            mAuth.sendPasswordResetEmail(email)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d("Reset", "Email sent.");
                                                                Toast.makeText(getContext(), "Email has been sent to " + email, LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    })
                                    .setText("Provided password is incorrect")
                                    .setDuration(ChocoBar.LENGTH_LONG)
                                    .build()
                                    .show();
                        }


                    }
                });
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void register(){
        mAuth.createUserWithEmailAndPassword(editText1L.getText().toString(), editText2L.getText().toString())
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SIGN UP", "createUserWithEmail:success");
                            user = mAuth.getCurrentUser();
                            sendEmailVerification();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SIGN UP", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getContext(), "Register failed.",
                                    LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void home(){
        Intent intent = new Intent(getContext(), Main2Activity.class);
        startActivity(intent);
    }

    public void sendEmailVerification() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("email verification", "Email verification sent.");
                                mAuth.signOut();
                                hideKeyboard(getActivity());
                                ChocoBar.builder().setActivity(getActivity())
                                        .setText("We've sent a verification link to this email address. Please sign in when you verified your email.")
                                        .setDuration(ChocoBar.LENGTH_LONG)
                                        .build()
                                        .show();
                            }
                        }
                    });
        }
    }

    private boolean IsEmailVerified() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){
            if (user.isEmailVerified()) {
                Log.d("email verification", "Email is verified.");
                return true;
            } else {
                Log.d("email verification", "Email is not verified !.");
            }
        }

        return false;
    }

    private void signIn() {
        Log.i("signIn", "called");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("onActivityResult", "started");
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.i("handleSignInResult", "called");
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
            // Signed in successfully, show authenticated UI.
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("handleSignInResult", "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("firebaseAuthWithGoogle", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            home();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("firebaseAuthWithGoogle", "signInWithCredential:failure", task.getException());
//
                        }

                        // ...
                    }
                });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
