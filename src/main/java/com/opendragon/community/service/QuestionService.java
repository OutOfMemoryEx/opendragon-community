package com.opendragon.community.service;

import com.opendragon.community.exception.CustomizeErrorCode;
import com.opendragon.community.exception.CustomizeException;
import com.opendragon.community.dto.PageInformation;
import com.opendragon.community.dto.QuestionDTO;
import com.opendragon.community.mapper.QuestionMapper;
import com.opendragon.community.mapper.UserMapper;
import com.opendragon.community.model.Question;
import com.opendragon.community.model.QuestionExample;
import com.opendragon.community.model.UserExample;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author opend
 * @Date 2019/7/26
 */
@Service
public class QuestionService {
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;


    public PageInformation findWithRowboundsByCreatorId(int creatorId, long page, final long pageSize){
        long offset = (page -1)*pageSize;
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria().andCreatorEqualTo(creatorId);
        RowBounds rowBounds = new RowBounds((int)offset, (int)pageSize);
        List<Question> questions = questionMapper.selectByExampleWithBLOBsWithRowbounds(questionExample, rowBounds);
        ArrayList<QuestionDTO> questionDTOS = new ArrayList<>();

        for(Question question : questions){
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            UserExample userExample = new UserExample();
            userExample.createCriteria().andIdEqualTo(question.getCreator());
            questionDTO.setUser(userMapper.selectByExample(userExample).get(0));
            questionDTOS.add(questionDTO);
        }
        questionExample.clear();
        questionExample.createCriteria().andCreatorEqualTo(creatorId);
        PageInformation pageInformation = new PageInformation(page, pageSize, questionMapper.countByExample(questionExample));
        pageInformation.setQuestionDTOs(questionDTOS);

        return pageInformation;
    }

    public PageInformation findWithRowbounds(long page, final long pageSize){
        long offset = (page-1)*pageSize;
        RowBounds rowBounds = new RowBounds((int)offset, (int)pageSize);
        List<Question> questions = questionMapper.selectByExampleWithBLOBsWithRowbounds(null, rowBounds);
        ArrayList<QuestionDTO> questionDTOS = new ArrayList<>();

        for(Question question : questions){
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question, questionDTO);
            UserExample userExample = new UserExample();
            userExample.createCriteria().andIdEqualTo(question.getCreator());
            questionDTO.setUser(userMapper.selectByExample(userExample).get(0));
            questionDTOS.add(questionDTO);
        }

        PageInformation pageInformation = new PageInformation(page, pageSize, questionMapper.countByExample(null));
        pageInformation.setQuestionDTOs(questionDTOS);

        return pageInformation;
    }


    public QuestionDTO findDTOById(Integer id) {
        Question question = questionMapper.selectByPrimaryKey(id);

        if(question == null){
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FIND);
        }

        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question, questionDTO);
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdEqualTo(question.getCreator());
        questionDTO.setUser(userMapper.selectByExample(userExample).get(0));
        return questionDTO;
    }

    public void addQuestionOrUpdateQuestion(Question question){
        if(question.getId() == 0){
            questionMapper.insert(question);
        }else{
            QuestionExample questionExample = new QuestionExample();
            questionExample.createCriteria().andIdEqualTo(question.getId());
            int update = questionMapper.updateByExampleSelective(question, questionExample);
            if(update != 1){
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FIND);
            }
        }
    }

    public Question findById(Integer id){
        return questionMapper.selectByPrimaryKey(id);
    }

    public void incrementViewCount(Integer id) {
        Question oldQuestion = questionMapper.selectByPrimaryKey(id);
        oldQuestion.setViewCount(oldQuestion.getViewCount()+1);
        questionMapper.updateByPrimaryKeySelective(oldQuestion);
    }
}
