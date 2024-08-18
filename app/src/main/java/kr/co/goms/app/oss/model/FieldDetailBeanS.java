package kr.co.goms.app.oss.model;

import android.os.Parcel;
import android.os.Parcelable;

public class FieldDetailBeanS implements Parcelable {
    private String res_mh_field_d_idx;
    private String res_mh_field_idx;
    private String res_mh_num;
    private String res_mh_date;
    private String res_mh_coordinate;
    private String res_mh_depth;
    private String res_mh_inflow;
    private String res_mh_outflow;
    private String res_mh_drainage;
    private String res_mh_standard;
    private String res_mh_material;
    private String res_mh_size;
    private String res_mh_lid_damage_yn;
    private String res_mh_lid_crack_yn;
    private String res_mh_lid_water_lms;
    private String res_mh_lid_damage_lms;
    private String res_mh_lid_crack_lms;
    private String res_mh_outer_damage_yn;
    private String res_mh_outer_crack_yn;
    private String res_mh_outer_water_lms;
    private String res_mh_outer_damage_lms;
    private String res_mh_outer_crack_lms;
    private String res_mh_inner_damage_yn;
    private String res_mh_inner_crack_yn;
    private String res_mh_inner_water_lms;
    private String res_mh_inner_damage_lms;
    private String res_mh_inner_crack_lms;
    private String res_mh_pipe_damage_yn;
    private String res_mh_pipe_crack_yn;
    private String res_mh_pipe_damage_lms;
    private String res_mh_pipe_crack_lms;
    private String res_mh_pipe_water_lms;
    private String res_mh_ladder_yn;
    private String res_mh_invert_yn;
    private String res_mh_odor_glms;
    private String res_mh_lid_sealing_yn;
    private String res_mh_block_gap_lms;
    private String res_mh_block_damage_lms;
    private String res_mh_block_leave_lms;
    private String res_mh_surface_gap_lms;
    private String res_mh_ladder_damage_lms;
    private String res_mh_endothelium_lms;
    private String res_mh_wasteoil_lms;
    private String res_mh_temp_obstacle_glms;
    private String res_mh_root_intrusion_glms;
    private String res_mh_cad;
    private String res_mh_photo_around;
    private String res_mh_photo_outer;
    private String res_mh_photo_inner;
    private String res_mh_photo_etc;
    private String res_mh_remark;
    private String res_mh_local_sp;
    private String res_mh_local_ep;
    private String res_mh_local_species;
    private String res_mh_local_circumference;
    private String res_mh_local_extension;
    private String res_mh_local_bigo;
    private String res_mh_buried_yn;

    public FieldDetailBeanS(){

    }


    protected FieldDetailBeanS(Parcel in) {
        res_mh_field_d_idx = in.readString();
        res_mh_field_idx = in.readString();
        res_mh_num = in.readString();
        res_mh_date = in.readString();
        res_mh_coordinate = in.readString();
        res_mh_depth = in.readString();
        res_mh_inflow = in.readString();
        res_mh_outflow = in.readString();
        res_mh_drainage = in.readString();
        res_mh_standard = in.readString();
        res_mh_material = in.readString();
        res_mh_size = in.readString();
        res_mh_lid_damage_yn = in.readString();
        res_mh_lid_crack_yn = in.readString();
        res_mh_lid_water_lms = in.readString();
        res_mh_lid_damage_lms = in.readString();
        res_mh_lid_crack_lms = in.readString();
        res_mh_outer_damage_yn = in.readString();
        res_mh_outer_crack_yn = in.readString();
        res_mh_outer_water_lms = in.readString();
        res_mh_outer_damage_lms = in.readString();
        res_mh_outer_crack_lms = in.readString();
        res_mh_inner_damage_yn = in.readString();
        res_mh_inner_crack_yn = in.readString();
        res_mh_inner_water_lms = in.readString();
        res_mh_inner_damage_lms = in.readString();
        res_mh_inner_crack_lms = in.readString();
        res_mh_pipe_damage_yn = in.readString();
        res_mh_pipe_crack_yn = in.readString();
        res_mh_pipe_damage_lms = in.readString();
        res_mh_pipe_crack_lms = in.readString();
        res_mh_pipe_water_lms = in.readString();
        res_mh_ladder_yn = in.readString();
        res_mh_invert_yn = in.readString();
        res_mh_odor_glms = in.readString();
        res_mh_lid_sealing_yn = in.readString();
        res_mh_block_gap_lms = in.readString();
        res_mh_block_damage_lms = in.readString();
        res_mh_block_leave_lms = in.readString();
        res_mh_surface_gap_lms = in.readString();
        res_mh_ladder_damage_lms = in.readString();
        res_mh_endothelium_lms = in.readString();
        res_mh_wasteoil_lms = in.readString();
        res_mh_temp_obstacle_glms = in.readString();
        res_mh_root_intrusion_glms = in.readString();
        res_mh_cad = in.readString();
        res_mh_photo_around = in.readString();
        res_mh_photo_outer = in.readString();
        res_mh_photo_inner = in.readString();
        res_mh_photo_etc = in.readString();
        res_mh_remark = in.readString();
        res_mh_local_sp = in.readString();
        res_mh_local_ep = in.readString();
        res_mh_local_species = in.readString();
        res_mh_local_circumference = in.readString();
        res_mh_local_extension = in.readString();
        res_mh_local_bigo = in.readString();
        res_mh_buried_yn = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(res_mh_field_d_idx);
        dest.writeString(res_mh_field_idx);
        dest.writeString(res_mh_num);
        dest.writeString(res_mh_date);
        dest.writeString(res_mh_coordinate);
        dest.writeString(res_mh_depth);
        dest.writeString(res_mh_inflow);
        dest.writeString(res_mh_outflow);
        dest.writeString(res_mh_drainage);
        dest.writeString(res_mh_standard);
        dest.writeString(res_mh_material);
        dest.writeString(res_mh_size);
        dest.writeString(res_mh_lid_damage_yn);
        dest.writeString(res_mh_lid_crack_yn);
        dest.writeString(res_mh_lid_water_lms);
        dest.writeString(res_mh_lid_damage_lms);
        dest.writeString(res_mh_lid_crack_lms);
        dest.writeString(res_mh_outer_damage_yn);
        dest.writeString(res_mh_outer_crack_yn);
        dest.writeString(res_mh_outer_water_lms);
        dest.writeString(res_mh_outer_damage_lms);
        dest.writeString(res_mh_outer_crack_lms);
        dest.writeString(res_mh_inner_damage_yn);
        dest.writeString(res_mh_inner_crack_yn);
        dest.writeString(res_mh_inner_water_lms);
        dest.writeString(res_mh_inner_damage_lms);
        dest.writeString(res_mh_inner_crack_lms);
        dest.writeString(res_mh_pipe_damage_yn);
        dest.writeString(res_mh_pipe_crack_yn);
        dest.writeString(res_mh_pipe_damage_lms);
        dest.writeString(res_mh_pipe_crack_lms);
        dest.writeString(res_mh_pipe_water_lms);
        dest.writeString(res_mh_ladder_yn);
        dest.writeString(res_mh_invert_yn);
        dest.writeString(res_mh_odor_glms);
        dest.writeString(res_mh_lid_sealing_yn);
        dest.writeString(res_mh_block_gap_lms);
        dest.writeString(res_mh_block_damage_lms);
        dest.writeString(res_mh_block_leave_lms);
        dest.writeString(res_mh_surface_gap_lms);
        dest.writeString(res_mh_ladder_damage_lms);
        dest.writeString(res_mh_endothelium_lms);
        dest.writeString(res_mh_wasteoil_lms);
        dest.writeString(res_mh_temp_obstacle_glms);
        dest.writeString(res_mh_root_intrusion_glms);
        dest.writeString(res_mh_cad);
        dest.writeString(res_mh_photo_around);
        dest.writeString(res_mh_photo_outer);
        dest.writeString(res_mh_photo_inner);
        dest.writeString(res_mh_photo_etc);
        dest.writeString(res_mh_remark);
        dest.writeString(res_mh_local_sp);
        dest.writeString(res_mh_local_ep);
        dest.writeString(res_mh_local_species);
        dest.writeString(res_mh_local_circumference);
        dest.writeString(res_mh_local_extension);
        dest.writeString(res_mh_local_bigo);
        dest.writeString(res_mh_buried_yn);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FieldDetailBeanS> CREATOR = new Creator<FieldDetailBeanS>() {
        @Override
        public FieldDetailBeanS createFromParcel(Parcel in) {
            return new FieldDetailBeanS(in);
        }

        @Override
        public FieldDetailBeanS[] newArray(int size) {
            return new FieldDetailBeanS[size];
        }
    };

    public String getRes_mh_field_d_idx() {
        return res_mh_field_d_idx;
    }

    public void setRes_mh_field_d_idx(String res_mh_field_d_idx) {
        this.res_mh_field_d_idx = res_mh_field_d_idx;
    }

    public String getRes_mh_field_idx() {
        return res_mh_field_idx;
    }

    public void setRes_mh_field_idx(String res_mh_field_idx) {
        this.res_mh_field_idx = res_mh_field_idx;
    }

    public String getRes_mh_num() {
        return res_mh_num;
    }

    public void setRes_mh_num(String res_mh_num) {
        this.res_mh_num = res_mh_num;
    }

    public String getRes_mh_date() {
        return res_mh_date;
    }

    public void setRes_mh_date(String res_mh_date) {
        this.res_mh_date = res_mh_date;
    }

    public String getRes_mh_coordinate() {
        return res_mh_coordinate;
    }

    public void setRes_mh_coordinate(String res_mh_coordinate) {
        this.res_mh_coordinate = res_mh_coordinate;
    }

    public String getRes_mh_depth() {
        return res_mh_depth;
    }

    public void setRes_mh_depth(String res_mh_depth) {
        this.res_mh_depth = res_mh_depth;
    }

    public String getRes_mh_inflow() {
        return res_mh_inflow;
    }

    public void setRes_mh_inflow(String res_mh_inflow) {
        this.res_mh_inflow = res_mh_inflow;
    }

    public String getRes_mh_outflow() {
        return res_mh_outflow;
    }

    public void setRes_mh_outflow(String res_mh_outflow) {
        this.res_mh_outflow = res_mh_outflow;
    }

    public String getRes_mh_drainage() {
        return res_mh_drainage;
    }

    public void setRes_mh_drainage(String res_mh_drainage) {
        this.res_mh_drainage = res_mh_drainage;
    }

    public String getRes_mh_standard() {
        return res_mh_standard;
    }

    public void setRes_mh_standard(String res_mh_standard) {
        this.res_mh_standard = res_mh_standard;
    }

    public String getRes_mh_material() {
        return res_mh_material;
    }

    public void setRes_mh_material(String res_mh_material) {
        this.res_mh_material = res_mh_material;
    }

    public String getRes_mh_size() {
        return res_mh_size;
    }

    public void setRes_mh_size(String res_mh_size) {
        this.res_mh_size = res_mh_size;
    }


    public String getRes_mh_lid_damage_yn() {
        return res_mh_lid_damage_yn;
    }

    public void setRes_mh_lid_damage_yn(String res_mh_lid_damage_yn) {
        this.res_mh_lid_damage_yn = res_mh_lid_damage_yn;
    }

    public String getRes_mh_lid_crack_yn() {
        return res_mh_lid_crack_yn;
    }

    public void setRes_mh_lid_crack_yn(String res_mh_lid_crack_yn) {
        this.res_mh_lid_crack_yn = res_mh_lid_crack_yn;
    }

    public String getRes_mh_lid_water_lms() {
        return res_mh_lid_water_lms;
    }

    public void setRes_mh_lid_water_lms(String res_mh_lid_water_lms) {
        this.res_mh_lid_water_lms = res_mh_lid_water_lms;
    }

    public String getRes_mh_lid_damage_lms() {
        return res_mh_lid_damage_lms;
    }

    public void setRes_mh_lid_damage_lms(String res_mh_lid_damage_lms) {
        this.res_mh_lid_damage_lms = res_mh_lid_damage_lms;
    }

    public String getRes_mh_lid_crack_lms() {
        return res_mh_lid_crack_lms;
    }

    public void setRes_mh_lid_crack_lms(String res_mh_lid_crack_lms) {
        this.res_mh_lid_crack_lms = res_mh_lid_crack_lms;
    }

    public String getRes_mh_outer_damage_yn() {
        return res_mh_outer_damage_yn;
    }

    public void setRes_mh_outer_damage_yn(String res_mh_outer_damage_yn) {
        this.res_mh_outer_damage_yn = res_mh_outer_damage_yn;
    }

    public String getRes_mh_outer_crack_yn() {
        return res_mh_outer_crack_yn;
    }

    public void setRes_mh_outer_crack_yn(String res_mh_outer_crack_yn) {
        this.res_mh_outer_crack_yn = res_mh_outer_crack_yn;
    }

    public String getRes_mh_outer_water_lms() {
        return res_mh_outer_water_lms;
    }

    public void setRes_mh_outer_water_lms(String res_mh_outer_water_lms) {
        this.res_mh_outer_water_lms = res_mh_outer_water_lms;
    }

    public String getRes_mh_outer_damage_lms() {
        return res_mh_outer_damage_lms;
    }

    public void setRes_mh_outer_damage_lms(String res_mh_outer_damage_lms) {
        this.res_mh_outer_damage_lms = res_mh_outer_damage_lms;
    }

    public String getRes_mh_outer_crack_lms() {
        return res_mh_outer_crack_lms;
    }

    public void setRes_mh_outer_crack_lms(String res_mh_outer_crack_lms) {
        this.res_mh_outer_crack_lms = res_mh_outer_crack_lms;
    }

    public String getRes_mh_inner_damage_yn() {
        return res_mh_inner_damage_yn;
    }

    public void setRes_mh_inner_damage_yn(String res_mh_inner_damage_yn) {
        this.res_mh_inner_damage_yn = res_mh_inner_damage_yn;
    }

    public String getRes_mh_inner_crack_yn() {
        return res_mh_inner_crack_yn;
    }

    public void setRes_mh_inner_crack_yn(String res_mh_inner_crack_yn) {
        this.res_mh_inner_crack_yn = res_mh_inner_crack_yn;
    }

    public String getRes_mh_inner_water_lms() {
        return res_mh_inner_water_lms;
    }

    public void setRes_mh_inner_water_lms(String res_mh_inner_water_lms) {
        this.res_mh_inner_water_lms = res_mh_inner_water_lms;
    }

    public String getRes_mh_inner_damage_lms() {
        return res_mh_inner_damage_lms;
    }

    public void setRes_mh_inner_damage_lms(String res_mh_inner_damage_lms) {
        this.res_mh_inner_damage_lms = res_mh_inner_damage_lms;
    }

    public String getRes_mh_inner_crack_lms() {
        return res_mh_inner_crack_lms;
    }

    public void setRes_mh_inner_crack_lms(String res_mh_inner_crack_lms) {
        this.res_mh_inner_crack_lms = res_mh_inner_crack_lms;
    }

    public String getRes_mh_pipe_damage_yn() {
        return res_mh_pipe_damage_yn;
    }

    public void setRes_mh_pipe_damage_yn(String res_mh_pipe_damage_yn) {
        this.res_mh_pipe_damage_yn = res_mh_pipe_damage_yn;
    }

    public String getRes_mh_pipe_crack_yn() {
        return res_mh_pipe_crack_yn;
    }

    public void setRes_mh_pipe_crack_yn(String res_mh_pipe_crack_yn) {
        this.res_mh_pipe_crack_yn = res_mh_pipe_crack_yn;
    }

    public String getRes_mh_pipe_damage_lms() {
        return res_mh_pipe_damage_lms;
    }

    public void setRes_mh_pipe_damage_lms(String res_mh_pipe_damage_lms) {
        this.res_mh_pipe_damage_lms = res_mh_pipe_damage_lms;
    }

    public String getRes_mh_pipe_crack_lms() {
        return res_mh_pipe_crack_lms;
    }

    public void setRes_mh_pipe_crack_lms(String res_mh_pipe_crack_lms) {
        this.res_mh_pipe_crack_lms = res_mh_pipe_crack_lms;
    }

    public String getRes_mh_pipe_water_lms() {
        return res_mh_pipe_water_lms;
    }

    public void setRes_mh_pipe_water_lms(String res_mh_pipe_water_lms) {
        this.res_mh_pipe_water_lms = res_mh_pipe_water_lms;
    }

    public String getRes_mh_ladder_yn() {
        return res_mh_ladder_yn;
    }

    public void setRes_mh_ladder_yn(String res_mh_ladder_yn) {
        this.res_mh_ladder_yn = res_mh_ladder_yn;
    }

    public String getRes_mh_invert_yn() {
        return res_mh_invert_yn;
    }

    public void setRes_mh_invert_yn(String res_mh_invert_yn) {
        this.res_mh_invert_yn = res_mh_invert_yn;
    }

    public String getRes_mh_odor_glms() {
        return res_mh_odor_glms;
    }

    public void setRes_mh_odor_glms(String res_mh_odor_glms) {
        this.res_mh_odor_glms = res_mh_odor_glms;
    }

    public String getRes_mh_lid_sealing_yn() {
        return res_mh_lid_sealing_yn;
    }

    public void setRes_mh_lid_sealing_yn(String res_mh_lid_sealing_yn) {
        this.res_mh_lid_sealing_yn = res_mh_lid_sealing_yn;
    }

    public String getRes_mh_block_gap_lms() {
        return res_mh_block_gap_lms;
    }

    public void setRes_mh_block_gap_lms(String res_mh_block_gap_lms) {
        this.res_mh_block_gap_lms = res_mh_block_gap_lms;
    }

    public String getRes_mh_block_damage_lms() {
        return res_mh_block_damage_lms;
    }

    public void setRes_mh_block_damage_lms(String res_mh_block_damage_lms) {
        this.res_mh_block_damage_lms = res_mh_block_damage_lms;
    }

    public String getRes_mh_block_leave_lms() {
        return res_mh_block_leave_lms;
    }

    public void setRes_mh_block_leave_lms(String res_mh_block_leave_lms) {
        this.res_mh_block_leave_lms = res_mh_block_leave_lms;
    }

    public String getRes_mh_surface_gap_lms() {
        return res_mh_surface_gap_lms;
    }

    public void setRes_mh_surface_gap_lms(String res_mh_surface_gap_lms) {
        this.res_mh_surface_gap_lms = res_mh_surface_gap_lms;
    }

    public String getRes_mh_ladder_damage_lms() {
        return res_mh_ladder_damage_lms;
    }

    public void setRes_mh_ladder_damage_lms(String res_mh_ladder_damage_lms) {
        this.res_mh_ladder_damage_lms = res_mh_ladder_damage_lms;
    }

    public String getRes_mh_endothelium_lms() {
        return res_mh_endothelium_lms;
    }

    public void setRes_mh_endothelium_lms(String res_mh_endothelium_lms) {
        this.res_mh_endothelium_lms = res_mh_endothelium_lms;
    }

    public String getRes_mh_wasteoil_lms() {
        return res_mh_wasteoil_lms;
    }

    public void setRes_mh_wasteoil_lms(String res_mh_wasteoil_lms) {
        this.res_mh_wasteoil_lms = res_mh_wasteoil_lms;
    }

    public String getRes_mh_temp_obstacle_glms() {
        return res_mh_temp_obstacle_glms;
    }

    public void setRes_mh_temp_obstacle_glms(String res_mh_temp_obstacle_glms) {
        this.res_mh_temp_obstacle_glms = res_mh_temp_obstacle_glms;
    }

    public String getRes_mh_root_intrusion_glms() {
        return res_mh_root_intrusion_glms;
    }

    public void setRes_mh_root_intrusion_glms(String res_mh_root_intrusion_glms) {
        this.res_mh_root_intrusion_glms = res_mh_root_intrusion_glms;
    }

    public String getRes_mh_cad() {
        return res_mh_cad;
    }

    public void setRes_mh_cad(String res_mh_cad) {
        this.res_mh_cad = res_mh_cad;
    }

    public String getRes_mh_photo_around() {
        return res_mh_photo_around;
    }

    public void setRes_mh_photo_around(String res_mh_photo_around) {
        this.res_mh_photo_around = res_mh_photo_around;
    }

    public String getRes_mh_photo_outer() {
        return res_mh_photo_outer;
    }

    public void setRes_mh_photo_outer(String res_mh_photo_outer) {
        this.res_mh_photo_outer = res_mh_photo_outer;
    }

    public String getRes_mh_photo_inner() {
        return res_mh_photo_inner;
    }

    public void setRes_mh_photo_inner(String res_mh_photo_inner) {
        this.res_mh_photo_inner = res_mh_photo_inner;
    }

    public String getRes_mh_photo_etc() {
        return res_mh_photo_etc;
    }

    public void setRes_mh_photo_etc(String res_mh_photo_etc) {
        this.res_mh_photo_etc = res_mh_photo_etc;
    }

    public String getRes_mh_remark() {
        return res_mh_remark;
    }

    public void setRes_mh_remark(String res_mh_remark) {
        this.res_mh_remark = res_mh_remark;
    }

    public String getRes_mh_local_sp() {
        return res_mh_local_sp;
    }

    public void setRes_mh_local_sp(String res_mh_local_sp) {
        this.res_mh_local_sp = res_mh_local_sp;
    }

    public String getRes_mh_local_ep() {
        return res_mh_local_ep;
    }

    public void setRes_mh_local_ep(String res_mh_local_ep) {
        this.res_mh_local_ep = res_mh_local_ep;
    }

    public String getRes_mh_local_species() {
        return res_mh_local_species;
    }

    public void setRes_mh_local_species(String res_mh_local_species) {
        this.res_mh_local_species = res_mh_local_species;
    }

    public String getRes_mh_local_circumference() {
        return res_mh_local_circumference;
    }

    public void setRes_mh_local_circumference(String res_mh_local_circumference) {
        this.res_mh_local_circumference = res_mh_local_circumference;
    }

    public String getRes_mh_local_extension() {
        return res_mh_local_extension;
    }

    public void setRes_mh_local_extension(String res_mh_local_extension) {
        this.res_mh_local_extension = res_mh_local_extension;
    }

    public String getRes_mh_local_bigo() {
        return res_mh_local_bigo;
    }

    public void setRes_mh_local_bigo(String res_mh_local_bigo) {
        this.res_mh_local_bigo = res_mh_local_bigo;
    }

    public String getRes_mh_buried_yn() {
        return res_mh_buried_yn;
    }

    public void setRes_mh_buried_yn(String res_mh_buried_yn) {
        this.res_mh_buried_yn = res_mh_buried_yn;
    }
}
