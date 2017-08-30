
package org.escola.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.aaf.dto.RecadoDTO;
import org.escola.enums.BimestreEnum;
import org.escola.enums.DisciplinaEnum;
import org.escola.model.AlunoAvaliacao;
import org.escola.model.Recado;
import org.escola.util.Service;


@Stateless
public class GradeService extends Service implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private Logger log;

	@PersistenceContext(unitName = "EscolaDS")
	private EntityManager em;
	
	

		
	public Map<BimestreEnum,Map<DisciplinaEnum,String>> notas;
	
	public  Map<BimestreEnum,Map<DisciplinaEnum,String>> getGrades(Long idAluno){
		Map<BimestreEnum,Map<DisciplinaEnum,String>> grades = new HashMap<>();
		
		
		
		//grades.put(BimestreEnum.PRIMEIRO_BIMESTRE, value)
		
		return grades;
	}
	
	public float getAVGrade(Long idAluno, DisciplinaEnum disciplina, BimestreEnum bimestre, boolean recupecacao) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT av from  AlunoAvaliacao av ");
		sql.append("where 1 = 1");
		sql.append(" and  av.aluno.id = ");
		sql.append(idAluno);
		sql.append(" and  av.avaliacao.disciplina = ");
		sql.append(disciplina.ordinal());
		if(bimestre != null){
			sql.append(" and  av.avaliacao.bimestre = ");
			sql.append(bimestre.ordinal());	
		}
		sql.append(" and  av.avaliacao.recuperacao = ");
		sql.append(recupecacao);
		Query query = em.createQuery(sql.toString());
		
		List<AlunoAvaliacao> notas = (List<AlunoAvaliacao>) query.getResultList();
		Float soma = 0F;
		Float pesos = 0F;
		if(notas != null && !notas.isEmpty()){
			for(AlunoAvaliacao avas : notas){
				soma += avas.getNota() * avas.getAvaliacao().getPeso();
				pesos += avas.getAvaliacao().getPeso();
			}
		}
		

		return soma/pesos;
	}

}

