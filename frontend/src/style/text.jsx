import Button from "@/components/common/Button"
import Card from "@/components/common/Card"


const Test = () => {

  return (
    <div>
      test
      <div>
        <h1>Button</h1>
      <Button 
        type={"DEFAULT"}
        text={"DEFAULT"}
        />
      <Button 
        type={"POSITIVE"}
        text={"POSITIVE"}
        />
      <Button 
        type={"NEGATIVE"}
        text={"NEGATIVE"}
        />
      </div>
      <div>
        <h1>Card</h1>
        <div className="card-container">
        <Card 
          type={"OnBoarding"}
          head_text1={"결혼을 결심하셨나요?"}
          head_text2={"정말 잘 하셨어요!"}
          sub_text1={"우리 부부만의 AI 비서가"}
          sub_text2={"결혼과 이후 과정까지 책임질게요"}
          icon={<img src="/icon/man.svg" alt="man icon" />}
          style={{backgroundColor: 'lightblue'}}
          />  
        <Card 
          type={"OnBoarding"} 
          head_text1={"같이 걸어볼까요?"}
          head_text2={"우리 부부만의 버진 로드"}
          sub_text1={"AI 비서가 우리 부부의 상황을 분석해"}
          sub_text2={"최적의 로드를 설계해 드릴게요"}
          icon={<img src="/icon/flower-bouquet.png" alt="flower-bouquet icon" />}
          style={{backgroundColor: 'lightgrey'}}
          />  
        <Card 
          type={"OnBoarding"}
          head_text1={"같이 걸어볼까요?"}
          head_text2={"우리 부부만의 버진 로드"}
          sub_text1={"AI 비서가 우리 부부의 상황을 분석해"}
          sub_text2={"최적의 로드를 설계해 드릴게요"}
          icon={<img src="/icon/heartwithribbon.svg" alt="heartwithribbon icon" />}
          style={{backgroundColor: '#FFEb3B'}}
          />  
          </div>
      </div>
    </div>
  )
}

export default Test 