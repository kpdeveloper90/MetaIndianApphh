package com.ecosense.app.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecosense.app.R;
import com.ecosense.app.helper.AppConfig;
import com.ecosense.app.helper.ConnactionCheckApplication;
import com.ecosense.app.helper.Connection;
import com.ecosense.app.helper.ItemClickListener;
import com.ecosense.app.helper.UserSessionManger;
import com.ecosense.app.pojo.SurveyQA;

import static com.ecosense.app.helper.AppConfig.PROGRASS_postDelayed;

public class Qua_Ans_ListAdapter extends RecyclerView.Adapter {
    private static final String TAG = Qua_Ans_ListAdapter.class.getSimpleName();
    private List<SurveyQA> surveyQAList;
    //    private List<Sasurvey_Answer> sasurvey_answer = null;
    //array answer
    JSONArray sasurvey_answer = null;
    private Context context;
    private ItemClickListener clickListener;
    UserSessionManger session = null;
    LinearLayoutManager linearLayoutManager;
    RecyclerView rv_QADetail;

    public Qua_Ans_ListAdapter(Context context, List<SurveyQA> surveyQAList, LinearLayoutManager linearLayoutManager, RecyclerView rv_QADetail) {
        this.surveyQAList = surveyQAList;
        this.context = context;
        this.linearLayoutManager = linearLayoutManager;
        this.rv_QADetail = rv_QADetail;
        sasurvey_answer = new JSONArray();
//        sasurvey_answer = new ArrayList<>();
        session = new UserSessionManger(this.context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_qua_ans_list_row, parent, false);

        return new MyViewHolder(itemView);
    }


    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (surveyQAList != null && 0 <= position && position < surveyQAList.size()) {

            final SurveyQA surveyItem = (SurveyQA) surveyQAList.get(position);

            //When Last question arrived  Show btn_Complete_Qu
            if (position == (surveyQAList.size() - 1)) {
                ((MyViewHolder) holder).btn_next_Qu.setVisibility(View.GONE);
                ((MyViewHolder) holder).btn_Complete_Qu.setVisibility(View.VISIBLE);
            }

//            if (surveyItem.getSuranstype().equalsIgnoreCase("Radio Option")) {
//                ((MyViewHolder) holder).ans_rg.setVisibility(View.VISIBLE);
//                ((MyViewHolder) holder).ans_survey_ratingView.setVisibility(View.GONE);
//            } else
            if (surveyItem.getSuranstype().equalsIgnoreCase("Rating")) {
                ((MyViewHolder) holder).ans_rg.setVisibility(View.GONE);
                ((MyViewHolder) holder).ans_survey_ratingView.setVisibility(View.VISIBLE);
            }
            ((MyViewHolder) holder).tv_survey_qua.setText("(" + surveyItem.getIdx() + ") " + surveyItem.getSurquestion());


            ((MyViewHolder) holder).btn_Complete_Qu.setOnClickListener(v -> {
                String answer = getQanswer(((MyViewHolder) holder), surveyItem);
                if (!answer.equalsIgnoreCase("None")) {
                    try {
                        JSONObject jo = new JSONObject();
                        jo.put("surandatetime", Connection.getCurrentDateTime());
                        jo.put("surananstype", surveyItem.getSuranstype());
                        jo.put("suranquestionid", surveyItem.getQAID());
                        jo.put("surananswer", answer);
                        sasurvey_answer.put(jo);

                        Log.e("QA Adapter ", "surveyId = " + surveyItem.getAasurveyid());

                        JSONObject jsonObject = new JSONObject();


                        jsonObject = new JSONObject();
                        jsonObject.put("aasurveyid", surveyItem.getAasurveyid());
                        jsonObject.put("saplatform", AppConfig.PLATFORM);
                        jsonObject.put("sauname", session.getpsName());
                        jsonObject.put("sauserid", session.getpsNo());
                        jsonObject.put("surstatus", AppConfig.CPTSTATUS_Complete);

                        jsonObject.put("sasurvey_answer", sasurvey_answer);

                        ((MyViewHolder) holder).ans_survey_ratingView.setSelectedSmile(BaseRating.NONE);

                        sendSurveyAnswertServer(jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSONObject Here", e.toString());
                    }
                } else {
                    TastyToast.makeText(context, "Please select Answer", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                }
            });
            ((MyViewHolder) holder).btn_next_Qu.setOnClickListener(v -> {
                String answer = getQanswer(((MyViewHolder) holder), surveyItem);

                    if (!answer.equalsIgnoreCase("None")) {
                    Log.e("btn_next_Qu ", "btn_next_Qu =  " + answer);
                    try {
                        JSONObject jo = new JSONObject();

                        jo.put("surandatetime", Connection.getCurrentDateTime());

                        jo.put("surananstype", surveyItem.getSuranstype());
                        jo.put("suranquestionid", surveyItem.getQAID());
                        jo.put("surananswer", answer);
                        sasurvey_answer.put(jo);

                        ((MyViewHolder) holder).ans_survey_ratingView.setSelectedSmile(BaseRating.NONE);
                        rv_QADetail.getLayoutManager().scrollToPosition(linearLayoutManager.findLastVisibleItemPosition() + 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    TastyToast.makeText(context, "Please select Answer", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                }
            });

        }
    }

    private String getQanswer(RecyclerView.ViewHolder holder, SurveyQA surveyItem) {
        String qaAnswer = "None";
//        if (surveyItem.getSuranstype().equalsIgnoreCase("Radio Option")) {
//
//            if (((MyViewHolder) holder).rb_no.isChecked()) {
//                qaAnswer = ((MyViewHolder) holder).rb_no.getText().toString();
//            } else {
//                qaAnswer = ((MyViewHolder) holder).rb_yes.getText().toString();
//            }
//        } else
        if (surveyItem.getSuranstype().equalsIgnoreCase("Rating")) {
            @BaseRating.Smiley int smiley = ((MyViewHolder) holder).ans_survey_ratingView.getSelectedSmile();
            switch (smiley) {
                case SmileRating.BAD:
                    qaAnswer = "Bad";
                    break;
                case SmileRating.GOOD:
                    qaAnswer = "Good";
                    break;
                case SmileRating.GREAT:
                    qaAnswer = "Great";
                    break;
                case SmileRating.OKAY:
                    qaAnswer = "Okay";
                    break;
                case SmileRating.TERRIBLE:
                    qaAnswer = "Terrible";
                    break;
                case SmileRating.NONE:
                    qaAnswer = "None";
                    break;
            }
//            int level = ((MyViewHolder) holder).ans_survey_ratingView.getRating();
//            Log.e("Adapter qaAnswer", "mSmileRating.getRating() level = " + level);
//        }

//            Log.e("Adapter Qa", "Qa = " + surveyItem.getSurquestion());
//            Log.e("Adapter qaAnswer", "qaAnswer = " + qaAnswer);
        }
        return qaAnswer;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        SmileRating ans_survey_ratingView;
        TextView tv_survey_qua;
        RadioGroup ans_rg;
        RadioButton rb_yes, rb_no;
        Button btn_next_Qu, btn_Complete_Qu;
        public Context context;


        public MyViewHolder(View view) {
            super(view);

            context = view.getContext();
            tv_survey_qua = (TextView) view.findViewById(R.id.tv_survey_qua);
            ans_survey_ratingView = (SmileRating) view.findViewById(R.id.ans_survey_ratingView);
            ans_rg = (RadioGroup) view.findViewById(R.id.ans_rg);
            rb_yes = (RadioButton) view.findViewById(R.id.rb_yes);
            rb_no = (RadioButton) view.findViewById(R.id.rb_no);
            btn_next_Qu = (Button) view.findViewById(R.id.btn_next_Qu);
            btn_Complete_Qu = (Button) view.findViewById(R.id.btn_Complete_Qu);

//            btn_Complete_Qu.setOnClickListener(this);
//            btn_next_Qu.setOnClickListener(this);
            rb_yes.setOnClickListener(this);
            rb_no.setOnClickListener(this);


            itemView.setTag(view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getAdapterPosition());
        }

    }

    @Override
    public int getItemCount() {
        return this.surveyQAList.size();
//        return 10;
    }

    public String dateFormat(String rdate) {

        String mStringDate = rdate;
        String oldFormat = "yyyy-MM-dd HH:mm:ss";
        String newFormat = "dd-MMM-yyyy hh:mm:ss a";

        String formatedDate = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(oldFormat);
        Date myDate = null;
        try {
            myDate = dateFormat.parse(mStringDate);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat(newFormat);
        formatedDate = timeFormat.format(myDate);

        return formatedDate;
    }


    public void setFilter(List<SurveyQA> complaintsListItems) {
        surveyQAList = new ArrayList<>();
        surveyQAList.addAll(complaintsListItems);
        notifyDataSetChanged();
    }

    ProgressDialog mProgressDialog;

    private void sendSurveyAnswertServer(JSONObject jsonObject) {

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(context.getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        new android.os.Handler().postDelayed(() -> {
            try {
                String data = null;
                data = "{}";
                URLEncoder.encode(data, "UTF-8");


//                String url = session.getMyServerIP() + "/api/resource/Complaints/" + URLEncoder.encode(complaintsDetails.getComId(), "UTF-8");
                String url = session.getMyServerIP() + "/api/resource/SurveyActivity";
                Log.e(TAG, "url = > " + url);
                StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                        response -> {
                            // response
                            if (!response.isEmpty()) {
                                if (!response.toString().equalsIgnoreCase("{}")) {
                                    Log.e("Response", response);
                                    try {
                                        //create ObjectMapper instance
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        JsonNode rootNode = objectMapper.readTree(response.toString());
                                        JsonNode statusData = rootNode.path("data");
                                        Log.e(TAG, "statusData = " + statusData.toString());
                                        if (!statusData.toString().trim().equalsIgnoreCase("{}")) {

                                            SurveyQA sqa = objectMapper.readValue(statusData.toString(), SurveyQA.class);

                                            Log.e(TAG, "Responce getComId = " + sqa.getQAID());

                                            if (mProgressDialog != null) {
                                                mProgressDialog.dismiss();
                                                mProgressDialog = null;
                                            }

                                            TastyToast.makeText(context, "Thank you for RoutePoint", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);

//                                            context.startActivity(new Intent(context, CitizenDashBoard.class));
                                            ((Activity) context).finish();
                                        } else {
                                            TastyToast.makeText(context, context.getString(R.string.could_no_send_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        if (mProgressDialog != null) {
                                            mProgressDialog.dismiss();
                                            mProgressDialog = null;

                                        }
                                    }
                                } else {
                                    TastyToast.makeText(context, context.getString(R.string.response_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);

                                }
                            } else {
                                Log.e(TAG, "Response Error");
                                TastyToast.makeText(context, context.getString(R.string.response_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);

                            }

                        }, error -> {
                    // error

                    if (mProgressDialog != null) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                    TastyToast.makeText(context, context.getString(R.string.network_error), TastyToast.LENGTH_SHORT, TastyToast.ERROR);

                    Log.e(TAG, " Error in response sendRegistrationRequestServer ");
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
//                        Gson gson = new Gson();
//                        String regpojo = gson.toJson(quaAns);
                        Log.e(TAG, "getParams   = " + jsonObject.toString());

                        params.put("data", jsonObject.toString());
                        return params;
                    }

                    @Override
                    protected VolleyError parseNetworkError(VolleyError volleyError) {
                        String json;
                        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
                            try {
                                json = new String(volleyError.networkResponse.data, HttpHeaderParser.parseCharset(volleyError.networkResponse.headers));
                            } catch (UnsupportedEncodingException e) {
                                return new VolleyError(e.getMessage());
                            }
                            return new VolleyError(json);
                        }
                        return volleyError;
                    }

                };
                String tag_string_req = "string_req";
                ConnactionCheckApplication.getInstance().addToRequestQueue(postRequest, tag_string_req);


            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }

            }
        }, PROGRASS_postDelayed);
    }

}

