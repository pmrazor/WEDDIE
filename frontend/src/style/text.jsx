import Button from "@/components/common/Button"



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
    </div>
  )
}

export default Test 