
package org.escola.service;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.aaf.financeiro.model.Pagador;
import org.aaf.financeiro.sicoob.util.CNAB240_SICOOB;
import org.aaf.financeiro.util.OfficeUtil;
import org.escola.enums.BimestreEnum;
import org.escola.enums.DisciplinaEnum;
import org.escola.model.Aluno;
import org.escola.model.AlunoAvaliacao;
import org.escola.model.Boleto;
import org.escola.model.ContratoAluno;
import org.escola.util.CONSTANTES;
import org.escola.util.Formatador;
import org.escola.util.Service;
import org.escola.util.Verificador;

@Stateless
public class AlunoService extends Service implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "EscolaDS")
	private EntityManager em;
	
	// TODO pegar da configuracao
		private int anoLetivo = 2018;

	public Aluno findById(Long id) {
		return em.find(Aluno.class, id);
	}

	public float getAVGrade(Long idAluno, DisciplinaEnum disciplina, BimestreEnum bimestre, boolean recupecacao) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT av from  AlunoAvaliacao av ");
		sql.append("where 1 = 1");
		sql.append(" and  av.aluno.id = ");
		sql.append(idAluno);
		sql.append(" and  av.avaliacao.disciplina = ");
		sql.append(disciplina.ordinal());
		if (bimestre != null) {
			sql.append(" and  av.avaliacao.bimestre = ");
			sql.append(bimestre.ordinal());
		}
		sql.append(" and  av.avaliacao.recuperacao = ");
		sql.append(recupecacao);
		Query query = em.createQuery(sql.toString());

		List<AlunoAvaliacao> notas = (List<AlunoAvaliacao>) query.getResultList();
		Float soma = 0F;
		Float pesos = 0F;
		if (notas != null && !notas.isEmpty()) {
			for (AlunoAvaliacao avas : notas) {
				soma += avas.getNota() * avas.getAvaliacao().getPeso();
				pesos += avas.getAvaliacao().getPeso();
			}
		}

		return soma / pesos;
	}

	public String enviarBoletoEmail(long idcrianca, int mesBoletoInt,String email) {
		Boleto bol = getBoletoMe(mesBoletoInt, idcrianca);
		byte[] anexoPDF = byteArrayPDFBoleto(getBoletoFinanceiro(bol), bol.getPagador(), bol.getContrato());
		System.out.println("xxpt 2 ");
		String corpoEmail = "<!DOCTYPE html><html><body><p><h2><center>Colégio Adonai.</center></h2><center>"
				+ "<a href=\"https://ibb.co/mF1WjR\"><img src=\"https://preview.ibb.co/dPMmJm/logo.jpg\" "
				+ "alt=\"logo\" border=\"0\" style=\"width:92px;height:92px;border:0;\" ></a><br/><br/></center>Prezado(a) #nomeResponsavel,"
				+ "<br/><p><br/><br/>Você esta recebendo o seu boleto do Colégio Adonai referente ao mês de <b><font size=\"2\" color=\"blue\"> #mesBoleto</font>"
				+ "</b> .<h3><center><font size=\"3\" color=\"blue\">Resumo da conta</font></center>"
				+ "</h3>Vencimento  :<font size=\"3\" color=\"blue\"> #vencimentoBoleto</font>"
				+ "<br/>Valor       :<font size=\"3\" color=\"blue\"> #valorAtualBoleto</font><br/><br/><br/><br/><center><h4>"
				+ "<font size=\"3\" color=\"red\"> Caso já tenha efetuado o pagamento favor desconsiderar esse e-mail. </font></h4></center></p><br/>"
				+ "<a href=\"https://ibb.co/jubLuR\"><img src=\"https://preview.ibb.co/bJMW16/assinatura_Email.png\" alt=\"assinatura_Email\" border=\"0\" "
				+ "style=\"width:365px;height:126px;border:0;\"></a></body></html>";
		corpoEmail = corpoEmail.replace("#vencimentoBoleto", Formatador.formataData(bol.getVencimento()));
		corpoEmail = corpoEmail.replace("#valorAtualBoleto", Formatador.valorFormatado(Verificador.getValorFinal(bol)));
		corpoEmail = corpoEmail.replace("#nomeResponsavel", bol.getContrato().getNomeResponsavel());
		corpoEmail = corpoEmail.replace("#mesBoleto", Formatador.getMes(bol.getVencimento()));

		System.out.println("xxpt 3 ");
		ByteArrayInputStream bais = new ByteArrayInputStream(anexoPDF);
		org.aaf.financeiro.util.EnviadorEmail.enviarEmail("Boleto - Colégio Adonai", corpoEmail, bais, email,
				CONSTANTES.emailFinanceiro, CONSTANTES.senhaEmailFinanceiro);
		System.out.println("xxpt 4 ");
		return null;
	}
	
	private org.aaf.financeiro.model.Boleto getBoletoFinanceiro(Boleto boleto) {
		org.aaf.financeiro.model.Boleto boletoFinanceiro = new org.aaf.financeiro.model.Boleto();
		boletoFinanceiro.setEmissao(boleto.getEmissao());
		boletoFinanceiro.setId(boleto.getId());
		boletoFinanceiro.setValorNominal(boleto.getValorNominal());
		boletoFinanceiro.setVencimento(boleto.getVencimento());
		boletoFinanceiro.setNossoNumero(String.valueOf(boleto.getNossoNumero()));
		boletoFinanceiro.setDataPagamento(OfficeUtil.retornaDataSomenteNumeros(boleto.getDataPagamento()));
		boletoFinanceiro.setValorPago(boleto.getValorPago());
		return boletoFinanceiro;
	}

	public byte[] byteArrayPDFBoleto(org.aaf.financeiro.model.Boleto boleto, Aluno aluno, ContratoAluno contrato) {
		Calendar c = Calendar.getInstance();
		c.setTime(boleto.getVencimento());
		CNAB240_SICOOB cnab = new CNAB240_SICOOB(2);

		Pagador pagador = new Pagador();
		pagador.setBairro(contrato.getBairro());
		pagador.setCep(contrato.getCep());
		pagador.setCidade(contrato.getCidade() != null ? contrato.getCidade() : "PALHOCA");
		pagador.setCpfCNPJ(contrato.getCpfResponsavel());
		pagador.setEndereco(contrato.getEndereco());
		pagador.setNome(contrato.getNomeResponsavel());
		pagador.setNossoNumero(boleto.getNossoNumero() + "");
		pagador.setUF("SC");
		List<org.aaf.financeiro.model.Boleto> boletos = new ArrayList<>();
		boletos.add(boleto);
		pagador.setBoletos(boletos);

		byte[] pdf = cnab.getBoletoPDF(pagador);

		return pdf;
	}

	public Boleto getBoletoMe(int mes, long aluno) {
		if (mes >= 0) {
			try {
				Calendar c = Calendar.getInstance();
				c.set(anoLetivo, mes, 1, 0, 0, 0);
				Calendar c2 = Calendar.getInstance();
				c2.set(anoLetivo, mes, c.getMaximum(Calendar.MONTH), 23, 59, 59);

				StringBuilder sql = new StringBuilder();
				sql.append("SELECT bol from Boleto bol ");
				sql.append("where 1=1 ");
				sql.append(" and bol.vencimento >= '");
				sql.append(c.getTime());
				sql.append("'");
				sql.append(" and bol.vencimento < '");
				sql.append(c2.getTime());
				sql.append("'");
				sql.append(" AND bol.pagador.removido = false ");
				sql.append(" AND (bol.cancelado = false ");
				sql.append(" or  bol.cancelado = null ) ");
				sql.append(" AND bol.pagador.id =  ");
				sql.append(aluno);
				
				System.out.println("QUERY:" + sql.toString());
				Query query = em.createQuery(sql.toString());
				Boleto boleto = (Boleto) query.getSingleResult();
				System.out.println("Boleto:" + boleto);
				
				return boleto;

			} catch (NoResultException nre) {
				return null;
			}
		}
		return null;

	}

}
