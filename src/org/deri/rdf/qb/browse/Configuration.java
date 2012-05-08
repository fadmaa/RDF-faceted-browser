package org.deri.rdf.qb.browse;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deri.rdf.browser.commands.RdfCommand;
import org.json.JSONException;
import org.json.JSONWriter;

public class Configuration extends RdfCommand {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String endpoint = request.getParameter("endpoint");
		response.setCharacterEncoding("UTF-8");

		response.setHeader("Content-Type", "application/json");
		response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
		response.setDateHeader("Expires", 0);

		Writer w = response.getWriter();
		JSONWriter writer = new JSONWriter(w);
		try{
			writer.object();
			writer.key("endpoint_url");
			writer.value(endpoint);
			writer.key("main_resource_selector");
			writer.value("a <http://purl.org/linked-data/cube#Observation> ");
			writer.key("script");
			writer.value("custom-script/QBViewer.js");
			writer.key("css");
			writer.value("custom-css/qb-style.css");
			writer.key("script_template");
			writer.value("viewObservation");
			writer.key("measures");
			writer.array();
			
			writer.value("http://purl.org/linked-data/sdmx/2009/measure#obsValue");
			writer.value("http://data.lod2.eu/scoreboard/properties/FOA_cit_Country_ofpubs");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_bfeu_IND_TOTAL_indilt1");
			writer.value("http://data.lod2.eu/scoreboard/properties/bb_ne_TOTAL_FBB_lines");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iday_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_ihif_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/FOA_ent_Country_ofpubs");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_adesucu_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/bb_lines_TOTAL_FBB_nbrlines");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iueduif_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iugm_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iusell_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iunw_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iuport_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_igovrt_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iugov_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_sisc_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iugov12_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_crman_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iugm_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/mbb_3gcov_TOTAL_POP_pop");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iueduif_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_broad_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_sisorp_ENT_L_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/bb_fcov_TOTAL_POP_pop");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_bfeu_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/mbb_penet_TOTAL_MBB_lines100");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_bgoodo_IND_TOTAL_indilt1");
			writer.value("http://data.lod2.eu/scoreboard/properties/h_iacc_HH_TOTAL_hh");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iuupl_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_bgoodo_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iuse_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iuif_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iunw_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iuupl_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iuif_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_ebuy_ENT_SM_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/bb_speed10_TOTAL_FBB_lines");
			writer.value("http://data.lod2.eu/scoreboard/properties/bb_speed2_TOTAL_FBB_lines");
			writer.value("http://data.lod2.eu/scoreboard/properties/bb_dsl_TOTAL_FBB_lines");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iugov_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_igovrt_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iuse_IND_RF_GE1_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_igov12rt_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iubk_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_igovpr_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/bb_fcov_RURAL_POP_pop");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iujob_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_esell_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_blt12_IND_TOTAL_indilt1");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_ebuy_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iusell_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iujob_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_esell_ENT_SM_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_igov_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_eturn_ENT_ALL_XFIN_turn");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_sisorp_ENT_SM_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_blt12_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iuolc_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iux_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_inv_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iu3g_IND_TOTAL_ind");
			writer.value("http://data.lod2.eu/scoreboard/properties/e_igovrt_ENT_ALL_XFIN_ent");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iubk_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/bb_penet_TOTAL_FBB_lines100");
			writer.value("http://data.lod2.eu/scoreboard/properties/h_broad_HH_TOTAL_hh");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_iuolc_IND_TOTAL_indiu3");
			writer.value("http://data.lod2.eu/scoreboard/properties/i_ihif_IND_TOTAL_ind");
			writer.endArray();
		// 	"template":
			writer.endObject();
		}catch(JSONException je){
			respondException(response, je);
		}
		w.close();

	}
	
}
