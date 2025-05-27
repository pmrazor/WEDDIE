import ButtonGroup from "./ButtonGroup"
import VirginRoadDomain from './domain/VirginRoadDomain';
import UserDomain from "./domain/UserDomain";

const ApiTest = () => {
  const virginRoadDomain = VirginRoadDomain();
  const userDomain = UserDomain();
  const domains = [userDomain, virginRoadDomain];

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">API 테스트</h1>
      {domains.map((domain, index) => (
        <ButtonGroup key={index} domain={domain.name} buttons={domain.buttons} />
      ))}
    </div>
  )
}

export default ApiTest